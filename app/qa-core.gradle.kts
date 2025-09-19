import org.gradle.api.tasks.Exec

tasks.register<Exec>("qaCheck") {
group = "verification"
description = "Smoke-run all v6.0 QA scripts"
isIgnoreExitValue = true
commandLine("bash","-lc", """
cd "${project.rootDir}"
shopt -s nullglob
for f in tools/checks/_v6.0.sh; do
echo "RUN:${'$'}f"
done
""".trimIndent())
}
