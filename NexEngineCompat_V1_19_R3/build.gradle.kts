plugins {
    id("su.nexmedia.project-conventions")
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    compileOnly(project(":NMS"))
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}
