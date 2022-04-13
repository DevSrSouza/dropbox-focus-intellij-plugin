package br.com.devsrsouza.intellij.dropboxfocus.services

import br.com.devsrsouza.intellij.dropboxfocus.actions.logger
import br.com.devsrsouza.intellij.dropboxfocus.psi.findGradlePropertySetValueOnCallback
import br.com.devsrsouza.intellij.dropboxfocus.psi.findGroovyHighOrderFunction
import br.com.devsrsouza.intellij.dropboxfocus.psi.findGroovyMethodCall
import br.com.devsrsouza.intellij.dropboxfocus.psi.findKotlinFunction
import br.com.devsrsouza.intellij.dropboxfocus.psi.forEachGroovyMethodCall
import br.com.devsrsouza.intellij.dropboxfocus.psi.forEachKotlinFunction
import br.com.devsrsouza.intellij.dropboxfocus.psi.getFirstArgumentAsLiteralString
import br.com.devsrsouza.intellij.dropboxfocus.psi.getFunctionArguments
import br.com.devsrsouza.intellij.dropboxfocus.psi.removeSurroundingQuotes
import com.android.tools.idea.util.toIoFile
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtOperationReferenceExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtTypeArgumentList
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.impl.stringValue
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

internal const val GRADLE_PROPERTY_SET_FUNCTION_NAME = "set"

internal const val SETTINGS_KTS_FILE = "settings.gradle.kts"
internal const val SETTINGS_FILE = "settings.gradle"
internal const val MODULE_SETTINGS_FILE_DEFAULT = "settings-all.gradle"

internal const val FOCUS_FILE_NAME_DEFAULT = ".focus"
private const val FOCUS_GROOVY_CALLBACK_NAME = "focus"
private const val KTS_CONFIGURE_CALLBACK_NAME = "configure"
private const val FOCUS_EXTENSION_FQN_TYPE_NAME = "com.dropbox.focus.FocusExtension"
private const val FOCUS_EXTENSION_SIMPLE_TYPE_NAME = "FocusExtension"
private const val ALL_SETTINGS_FILE_NAME_ASSIGNMENT_NAME = "allSettingsFileName"
private const val FOCUS_FILE_NAME_ASSIGNMENT_NAME = "focusFileName"
private const val MODULE_INCLUDE_FUNCTION_NAME = "include"
private const val PROJECT_FUNCTION_NAME = "project"
private const val SET_PROJECT_DIR_PROPERTY_NAME = "projectDir"
private const val GRADLE_PLUGINS_CALLBACK_NAME = "plugins"
private const val GRADLE_PLUGINS_ID_FUNCTION_NAME = "id"
private const val FOCUS_GRADLE_PLUGIN_ID = "com.dropbox.focus"

private val FOCUS_EXTENSION_TYPE_NAMES = listOf(FOCUS_EXTENSION_FQN_TYPE_NAME, FOCUS_EXTENSION_SIMPLE_TYPE_NAME)

data class FocusGradleSettings(
    val allModulesSettingsFile: String,
    val focusFileName: String,
    val currentFocusGradleIncludeFile: String?,
    val allModules: List<FocusModule>,
) {
    val currentFocusModulePath: String? get() {
        val moduleDir = currentFocusGradleIncludeFile?.removeSuffix("/build/focus.settings.gradle")
        return allModules.find { it.moduleDirRelativeToRootProjectDir == moduleDir }
            ?.gradleModulePath
    }
}

data class FocusModule(
    val gradleModulePath: String,
    val moduleDirPath: Path,
    val moduleDirRelativeToRootProjectDir: String,
)

@Service
class FocusSettingsReader(private val project: Project) {
    // TODO: settings cache and update cache with project gradle sync listener

    fun getProjectFocusSettings(): FocusGradleSettings? {
        val dir = project.guessProjectDir() ?: return null

        val gradleSettingsFile = File(dir.toIoFile(), SETTINGS_FILE).takeIf(File::exists)
            ?: File(dir.toIoFile(), SETTINGS_KTS_FILE).takeIf(File::exists)
            ?: return null

        return readSettings(gradleSettingsFile)
    }

    private fun readSettings(file: File): FocusGradleSettings? {
        val psiFile = file.toPsiFile(project) ?: return null

        return when (file.name) {
            SETTINGS_KTS_FILE -> readKotlinScriptSettings(psiFile)
            SETTINGS_FILE -> readGroovySettings(psiFile)
            else -> null
        }
    }

    fun readGroovySettings(psiFile: PsiFile): FocusGradleSettings? {
        logger.debug("Looking into settings.gradle (groovy) to find Focus configuration")
        val pluginsExtensionBlock = psiFile.findGroovyHighOrderFunction(GRADLE_PLUGINS_CALLBACK_NAME) ?: return null

        // We try to find in the plugins block a declaration for the Focus Plugin ID
        val isFocusPluginApplied = pluginsExtensionBlock.findGroovyMethodCall(GRADLE_PLUGINS_ID_FUNCTION_NAME) {
            it.getFirstArgumentAsLiteralString() == FOCUS_GRADLE_PLUGIN_ID
        } != null

        if (!isFocusPluginApplied) return null

        // The focus configuration block could not be present, meaning that the user is using the default
        // focus plugin setting resulting in focusExtensionBlock being null
        val focusExtensionBlock = psiFile.findGroovyHighOrderFunction(FOCUS_GROOVY_CALLBACK_NAME)

        val allSettingsFileName = focusExtensionBlock?.findDescendantOfType<GrAssignmentExpression> {
            it.lValue.text == ALL_SETTINGS_FILE_NAME_ASSIGNMENT_NAME
        }?.rValue?.stringValue() ?: MODULE_SETTINGS_FILE_DEFAULT

        val focusFileName = focusExtensionBlock?.findDescendantOfType<GrAssignmentExpression> {
            it.lValue.text == FOCUS_FILE_NAME_ASSIGNMENT_NAME
        }?.rValue?.stringValue() ?: FOCUS_FILE_NAME_DEFAULT

        return FocusGradleSettings(
            allModulesSettingsFile = allSettingsFileName,
            focusFileName = focusFileName,
            currentFocusGradleIncludeFile = getCurrentFocusOrNull(focusFileName),
            allModules = readAllModules(allSettingsFileName),
        )
    }

    fun readKotlinScriptSettings(psiFile: PsiFile): FocusGradleSettings? {
        logger.debug("Looking into settings.gradle.kts (kotlin) to find Focus configuration")
        val pluginsExtensionBlock = psiFile.findKotlinFunction(GRADLE_PLUGINS_CALLBACK_NAME) ?: return null

        val isFocusPluginApplied = pluginsExtensionBlock
            .findKotlinFunction(GRADLE_PLUGINS_ID_FUNCTION_NAME) { _, arguments ->
                arguments?.getFirstArgumentAsLiteralString() == FOCUS_GRADLE_PLUGIN_ID
            } != null

        if (!isFocusPluginApplied) {
            return null
        }

        // The focus configuration block could not be present, meaning that the user is using the default
        // focus plugin setting resulting in focusExtensionBlock being null
        val focusConfigureExtensionBlock = psiFile.findKotlinFunction(KTS_CONFIGURE_CALLBACK_NAME) { it, _ ->
            // check if the generic type is FocusExtension or com.dropbox.focus.FocusExtension
            // for example: configure<com.dropbox.focus.FocusExtension> {}
            it.children.asSequence().filterIsInstance<KtTypeArgumentList>()
                .any { (it.arguments.firstOrNull()?.text ?: "") in FOCUS_EXTENSION_TYPE_NAMES }
        }

        val allSettingsFileName = focusConfigureExtensionBlock
            ?.findGradlePropertySetValueOnCallback(ALL_SETTINGS_FILE_NAME_ASSIGNMENT_NAME)
            ?: MODULE_SETTINGS_FILE_DEFAULT
        val focusFileName = focusConfigureExtensionBlock
            ?.findGradlePropertySetValueOnCallback(FOCUS_FILE_NAME_ASSIGNMENT_NAME)
            ?: FOCUS_FILE_NAME_DEFAULT

        return FocusGradleSettings(
            allModulesSettingsFile = allSettingsFileName,
            focusFileName = focusFileName,
            currentFocusGradleIncludeFile = getCurrentFocusOrNull(focusFileName),
            allModules = readAllModules(allSettingsFileName),
        )
    }

    private fun readAllModules(allSettingsFileName: String): List<FocusModule> {
        val dir = project.guessProjectDir() ?: return emptyList()

        val allSettingsFile = File(dir.toIoFile(), allSettingsFileName).takeIf(File::exists) ?: return emptyList()
        val psiFile = allSettingsFile.toPsiFile(project) ?: return emptyList()

        return when (allSettingsFile.extension) {
            "kts" -> readAllModulesFromKts(psiFile)
            "gradle" -> readAllModulesFromGroovy(psiFile)
            else -> emptyList()
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun readAllModulesFromGroovy(psiFile: PsiFile): List<FocusModule> {
        val rootProjectDir = project.guessProjectDir()!!.toNioPath()
        fun String.pathAsModuleDir() = replace(":", "/").removePrefix("/")

        val modules = mutableMapOf<String, String>()
        psiFile.forEachGroovyMethodCall(MODULE_INCLUDE_FUNCTION_NAME) {
            val modulePath = it.getFirstArgumentAsLiteralString()

            if (modulePath != null) {
                modules += modulePath to modulePath.pathAsModuleDir()
            }
        }

        psiFile.forEachDescendantOfType<GrAssignmentExpression> {
            val modulePath = it.lValue.findGroovyMethodCall(PROJECT_FUNCTION_NAME)
                ?.getFunctionArguments()
                ?.getFirstArgumentAsLiteralString()
                ?: return@forEachDescendantOfType

            val isSetProjectDir = (it.lValue as? GrReferenceExpression)?.canonicalText == SET_PROJECT_DIR_PROPERTY_NAME

            if (isSetProjectDir) {
                val projectDirPath = it.rValue?.findDescendantOfType<GrLiteral>()?.stringValue()
                    ?: return@forEachDescendantOfType

                modules += modulePath to projectDirPath
            }
        }

        return modules.map { (modulePath, moduleDir) ->
            FocusModule(
                gradleModulePath = modulePath,
                moduleDirPath = rootProjectDir / moduleDir,
                moduleDirRelativeToRootProjectDir = moduleDir,
            )
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun readAllModulesFromKts(psiFile: PsiFile): List<FocusModule> {
        val rootProjectDir = project.guessProjectDir()!!.toNioPath()
        fun String.pathAsModuleDir() = replace(":", "/").removePrefix("/")
        val modules = mutableMapOf<String, String>()

        psiFile.forEachKotlinFunction(MODULE_INCLUDE_FUNCTION_NAME) { it, arguments ->
            val modulePath = arguments?.getFirstArgumentAsLiteralString()
            if (modulePath != null) {
                modules += modulePath to modulePath.pathAsModuleDir()
            }
        }

        psiFile.forEachKotlinFunction(PROJECT_FUNCTION_NAME) { it, arguments ->
            val modulePath = arguments?.getFirstArgumentAsLiteralString() ?: return@forEachKotlinFunction
            if (modulePath !in modules.keys) return@forEachKotlinFunction

            val projectDirExpression = it.parent as? KtDotQualifiedExpression ?: return@forEachKotlinFunction

            val isProjectDir = projectDirExpression.children.filterIsInstance<KtNameReferenceExpression>()
                .lastOrNull()?.text == SET_PROJECT_DIR_PROPERTY_NAME

            if (isProjectDir) {
                val setProjectDirExpression = projectDirExpression.parent as? KtBinaryExpression
                    ?: return@forEachKotlinFunction

                val resultsPsiElements = setProjectDirExpression.children.dropWhile { it !is KtOperationReferenceExpression }

                val projectDirPath = resultsPsiElements.asSequence().mapNotNull {
                    it.findDescendantOfType<KtStringTemplateExpression>()?.text?.removeSurroundingQuotes()
                }.firstOrNull() ?: return@forEachKotlinFunction

                modules += modulePath to projectDirPath
            }
        }

        return modules.map { (modulePath, moduleDir) ->
            FocusModule(
                gradleModulePath = modulePath,
                moduleDirPath = rootProjectDir / moduleDir,
                moduleDirRelativeToRootProjectDir = moduleDir,
            )
        }
    }

    private fun getCurrentFocusOrNull(focusFileName: String): String? {
        return project.guessProjectDir()?.toIoFile()?.let {
            File(it, focusFileName).takeIf(File::exists)?.readText()
        }
    }
}