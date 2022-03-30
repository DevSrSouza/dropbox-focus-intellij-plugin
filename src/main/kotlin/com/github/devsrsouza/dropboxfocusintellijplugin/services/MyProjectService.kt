package com.github.devsrsouza.dropboxfocusintellijplugin.services

import com.intellij.openapi.project.Project
import com.github.devsrsouza.dropboxfocusintellijplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
