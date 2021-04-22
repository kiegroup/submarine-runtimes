@Library('jenkins-pipeline-shared-libraries')_

import org.kie.jenkins.MavenCommand

changeAuthor = env.ghprbPullAuthorLogin ?: CHANGE_AUTHOR
changeBranch = env.ghprbSourceBranch ?: CHANGE_BRANCH
changeTarget = env.ghprbTargetBranch ?: CHANGE_TARGET

kogitoRuntimesRepo = 'kogito-runtimes'
optaplannerRepo = 'optaplanner'
kogitoAppsRepo = 'kogito-apps'
kogitoExamplesRepo = 'kogito-examples'

pipeline {
    agent any
    // agent {
    //     label 'kie-rhel7 && kie-mem16g'
    // }
    // tools {
    //     maven 'kie-maven-3.6.2'
    //     jdk 'kie-jdk11'
    // }
    options {
        timestamps()
        timeout(time: 600, unit: 'MINUTES')
    }
    environment {
        SONARCLOUD_TOKEN = credentials('SONARCLOUD_TOKEN')
        MAVEN_OPTS = '-Xms1024m -Xmx4g'
    }
    stages {
        stage('Initialize') {
            steps {
                script {
                    mailer.buildLogScriptPR()

                    checkoutRepo(kogitoRuntimesRepo)
                }
            }
        }
        // stage('Build quarkus') {
        //     when {
        //         expression { return getQuarkusBranch() }
        //     }
        //     steps {
        //         script {
        //             checkoutQuarkusRepo()
        //             runQuickBuild(getMavenCommand('quarkus', false))
        //         }
        //     }
        // }
        // stage('Runtimes Build&Test') {
        //     steps {
        //         script {
        //             if (isNormalPRCheck()) {
        //                 runUnitTests(kogitoRuntimesRepo, getMavenCommand(kogitoRuntimesRepo, true, true).withProfiles(['run-code-coverage']))
        //                 runSonarcloudAnalysis(getMavenCommand(kogitoRuntimesRepo, true, true))
        //             } else {
        //                 runUnitTests(kogitoRuntimesRepo, getMavenCommand(kogitoRuntimesRepo, true, true))
        //             }
        //         }
        //     }
        // }
        // stage('Runtimes integration-tests') {
        //     steps {
        //         script {
        //             runIntegrationTests(kogitoRuntimesRepo, getMavenCommand(kogitoRuntimesRepo, true, true))
        //         }
        //     }
        // }
        // stage('Runtimes integration-tests with persistence') {
        //     steps {
        //         script {
        //             runIntegrationTests(kogitoRuntimesRepo, getMavenCommand(kogitoRuntimesRepo, true, true), ['persistence'])
        //         }
        //     }
        // }
        // stage('OptaPlanner Build') {
        //     steps {
        //         script {
        //             runUnitTests(optaplannerRepo, getMavenCommand(optaplannerRepo, true, true))
        //         }
        //     }
        // }
        // stage('Apps Build&Test') {
        //     steps {
        //         script {
        //             mvncmd = getMavenCommand('kogito-apps', true, true)
        //                 .withProperty('skip.ui.build')
        //                 .withProperty('skip.ui.deps')
        //             runUnitTests(kogitoAppsRepo, mvncmd, 'clean install')
        //         }
        //     }
        // }
        // stage('Examples Build&Test') {
        //     steps {
        //         script {
        //             runUnitTests(kogitoExamplesRepo, getMavenCommand(kogitoExamplesRepo, true, true), 'clean install')
        //         }
        //     }
        // }
        // stage('Examples integration-tests with persistence') {
        //     steps {
        //         script {
        //             runIntegrationTests(kogitoExamplesRepo, getMavenCommand(kogitoExamplesRepo, true, true), ['persistence'])
        //         }
        //     }
        // }
        // stage('Check Examples with events') {
        //     steps {
        //         script {
        //             runIntegrationTests(kogitoExamplesRepo, getMavenCommand(kogitoExamplesRepo, true, true), ['events'])
        //         }
        //     }
        // }
    }
    post {
        always {
            script {
                sh '$WORKSPACE/trace.sh'
            }
        }
        failure {
            script {
                mailer.sendEmail_failedPR()
            }
        }
        unstable {
            script {
                mailer.sendEmail_unstablePR()
            }
        }
        fixed {
            script {
                mailer.sendEmail_fixedPR()
            }
        }
        cleanup {
            script {
                util.cleanNode('docker')
            }
        }
    }
}

void checkoutRepo(String repo) {
    if (repo == optaplannerRepo) {
        checkoutRepo(repo, changeAuthor, changeBranch, getOptaplannerReleaseBranch(changeTarget))
    } else {
        checkoutRepo(repo, changeAuthor, changeBranch, changeTarget)
    }
}

String getOptaplannerReleaseBranch(String branch) {
    String checkedBranch = branch
    String [] versionSplit = checkedBranch.split("\\.")
    if (versionSplit.length == 3
        && versionSplit[0].isNumber()
        && versionSplit[1].isNumber()
       && versionSplit[2] == 'x') {
        checkedBranch = "${Integer.parseInt(versionSplit[0]) + 7}.${versionSplit[1]}.x"
    } else {
        echo "Cannot parse branch as release branch so going further with current value: ${checkedBranch}"
       }
    return checkedBranch
}

void checkoutRepo(String repo, String author, String branch, String targetBranch = '') {
    dir(repo) {
        if (targetBranch) {
            githubscm.checkoutIfExists(repo, author, branch, 'kiegroup', targetBranch, true)
        } else {
            checkout(githubscm.resolveRepository(repo, author, branch, false))
        }
    }
}

void checkoutQuarkusRepo() {
    checkoutRepo('quarkus', 'quarkusio', getQuarkusBranch())
}

MavenCommand getMavenCommand(String directory, boolean addQuarkusVersion=true, boolean canNative = false) {
    mvnCmd = new MavenCommand(this, ['-fae'])
                .withSettingsXmlId('kogito_release_settings')
                .withSnapshotsDisabledInSettings()
                .inDirectory(directory)
    if (addQuarkusVersion && getQuarkusBranch()) {
        mvnCmd.withProperty('version.io.quarkus', '999-SNAPSHOT')
    }
    if (canNative && isNative()) {
        mvnCmd.withProfiles(['native'])
            .withProperty('quarkus.native.container-build', true)
            .withProperty('quarkus.native.container-runtime', 'docker')
            .withProperty('quarkus.profile', 'native') // Added due to https://github.com/quarkusio/quarkus/issues/13341
    }
    return mvnCmd
}

void saveReports(boolean allowEmptyResults = false) {
    junit testResults: '**/target/surefire-reports/**/*.xml, **/target/failsafe-reports/**/*.xml', allowEmptyResults: allowEmptyResults
}

void cleanContainers() {
    cloud.cleanContainersAndImages('docker')
}

String getQuarkusBranch() {
    return env['QUARKUS_BRANCH']
}

boolean isNative() {
    return env['NATIVE'] && env['NATIVE'].toBoolean()
}

boolean isNormalPRCheck() {
    return !(getQuarkusBranch() || isNative())
}

void runQuickBuild(MavenCommand mvnCmd) {
    mvnCmd.withProperty('quickly')
            .run('clean install')
}

void runUnitTests(String projectName, MavenCommand mvnCmd) {
    if (projectName == 'optaplanner') {
        mvnCmd.withProperty('enforcer.skip')
            .withProperty('formatter.skip')
            .withProperty('impsort.skip')
            .withProperty('revapi.skip')
    } else {
        mvnCmd.withProperty('quickTests')
    }

    runMavenTests(mvnCmd, 'clean install')
}

void runSonarcloudAnalysis(MavenCommand mvnCmd) {
    mvnCmd.withOptions(['-e', '-nsu'])
            .withProfiles(['sonarcloud-analysis'])
            .run('validate')
}

void runIntegrationTests(String projectName, MavenCommand mvnCmd, List profiles=[]) {
    String profileSuffix = profiles ? "-${profiles.join('-')}" : ''
    String itFolder = "${projectName}-it${profileSuffix}"
    sh "cp -r ${projectName} ${itFolder}"

    runMavenTests(mvnCmd.inDirectory(itFolder).withProfiles(profiles), 'verify')
}

void runMavenTests(MavenCommand mvnCmd, String mvnRunCmd) {
    try {
        mvnCmd.run(mvnRunCmd)
    } catch (err) {
        throw err
    } finally {
        saveReports()
        cleanContainers()
    }
}
