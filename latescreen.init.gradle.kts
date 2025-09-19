import org.gradle.api.tasks.Exec
gradle.beforeProject {
    if (path == ":app") {
        tasks.register("qaCheckLateScreen", Exec::class.java) {
            group = "qa"
            description = "Validate late-night screen CSV"
            commandLine("bash", "${project.projectDir}/tools/qa/late_screen_check.sh")
        }
    }
}
