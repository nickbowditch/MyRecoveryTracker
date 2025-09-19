tasks.register<Exec>("qaCheckLateScreen") {
    group = "qa"
    description = "Validates header/duplicates/range for daily_late_screen.csv"
    commandLine("bash", "tools/qa/late_screen_check.sh")
}
tasks.register<Exec>("qaVerifyWriterGuardLateScreen") {
    group = "qa"
    description = "Fails if non-whitelisted sources reference daily_late_screen.csv"
    commandLine("bash", "tools/qa/verify_latescreen_writers.sh")
}
tasks.register<Exec>("qaSealMetrics") {
    group = "qa"
    description = "Seals current on-device daily_metrics.csv header to app/locks/daily_metrics.header"
    commandLine("bash", "tools/qa/metrics_seal.sh")
}
tasks.register<Exec>("qaVerifyMetricsLateScreen") {
    group = "qa"
    description = "Verifies daily_metrics exists, has minimum cols (including late_night_screen_minutes), and matches sealed header"
    commandLine("bash", "tools/qa/metrics_verify.sh")
}
tasks.register("qaGovernanceLateScreen") {
    group = "qa"
    description = "Runs schema + writer + metrics governance checks for late-night screen usage"
    dependsOn("qaCheckLateScreen", "qaVerifyWriterGuardLateScreen", "qaVerifyMetricsLateScreen")
}
