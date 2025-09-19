tasks.register<Exec>("qaCheckLateScreen") {
    group = "qa"
    description = "Validates header/duplicates/range for daily_late_screen.csv"
    commandLine("bash", "tools/qa/late_screen_check.sh")
}
