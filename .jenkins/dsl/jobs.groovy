import org.kie.jenkins.jobdsl.templates.KogitoJobTemplate
import org.kie.jenkins.jobdsl.KogitoConstants
import org.kie.jenkins.jobdsl.Utils
import org.kie.jenkins.jobdsl.KogitoJobType

boolean isMainBranch() {
    return "${GIT_BRANCH}" == "${GIT_MAIN_BRANCH}"
}

def getDefaultJobParams() {
    return [
        job: [
            name: 'kogito-runtimes'
        ],
        git: [
            author: "${GIT_AUTHOR_NAME}",
            branch: "${GIT_BRANCH}",
            repository: 'kogito-runtimes',
            credentials: "${GIT_AUTHOR_CREDENTIALS_ID}",
            token_credentials: "${GIT_AUTHOR_TOKEN_CREDENTIALS_ID}"
        ]
    ]
}

def getJobParams(String jobName, String jobFolder, String jenkinsfileName, String jobDescription = '') {
    def jobParams = getDefaultJobParams()
    jobParams.job.name = jobName
    jobParams.job.folder = jobFolder
    jobParams.jenkinsfile = jenkinsfileName
    if (jobDescription) {
        jobParams.job.description = jobDescription
    }
    return jobParams
}

def bddRuntimesPrFolder = "${KogitoConstants.KOGITO_DSL_PULLREQUEST_FOLDER}/${KogitoConstants.KOGITO_DSL_RUNTIMES_BDD_FOLDER}"
def nightlyBranchFolder = "${KogitoConstants.KOGITO_DSL_NIGHTLY_FOLDER}/${JOB_BRANCH_FOLDER}"
def releaseBranchFolder = "${KogitoConstants.KOGITO_DSL_RELEASE_FOLDER}/${JOB_BRANCH_FOLDER}"

if (isMainBranch()) {
    folder(KogitoConstants.KOGITO_DSL_PULLREQUEST_FOLDER)

    setupPrsJob(KogitoConstants.KOGITO_DSL_PULLREQUEST_FOLDER)
    setupQuarkusLTSPrJob(KogitoConstants.KOGITO_DSL_PULLREQUEST_FOLDER)
    setupNativePrJob(KogitoConstants.KOGITO_DSL_PULLREQUEST_FOLDER)

    // For BDD runtimes PR job
    folder(bddRuntimesPrFolder)

    setupDeployJob(bddRuntimesPrFolder, KogitoJobType.PR)
}

// Nightly jobs
folder(KogitoConstants.KOGITO_DSL_NIGHTLY_FOLDER)
folder(nightlyBranchFolder)
if (isMainBranch()) {
    setupDroolsJob(nightlyBranchFolder)

    setupQuarkusJob(nightlyBranchFolder, 'main')
    setupQuarkusJob(nightlyBranchFolder, "${QUARKUS_LTS_VERSION}")
}
setupSonarCloudJob(nightlyBranchFolder)
setupNativeJob(nightlyBranchFolder)
setupDeployJob(nightlyBranchFolder, KogitoJobType.NIGHTLY)
setupPromoteJob(nightlyBranchFolder, KogitoJobType.NIGHTLY)

// No release directly on main branch
if (!isMainBranch()) {
    folder(KogitoConstants.KOGITO_DSL_RELEASE_FOLDER)
    folder(releaseBranchFolder)

    setupDeployJob(releaseBranchFolder, KogitoJobType.RELEASE)
    setupPromoteJob(releaseBranchFolder, KogitoJobType.RELEASE)
}

/////////////////////////////////////////////////////////////////
// Methods
/////////////////////////////////////////////////////////////////

void setupPrsJob(String jobFolder) {
    // Setup the different PR jobs

    // Unit tests
    setupPrJob(jobFolder) { jobParams ->
        jobParams.job.description = "Run unit tests from ${jobParams.job.name} repository"
        jobParams.job.name += '.unit-tests'
        jobParams.pr = [
            // TODO to change back the first phrase to `(.*[j|J]enkins,?.*(retest|test) this.*)`
            trigger_phrase : '(.*[j|J]enkins,?.*(retestesting) this.*)|(.*[j|J]enkins,? run [U|u]nit[ tests]?.*)',
            trigger_phrase_only: true, // TODO be removed once tests are finished
            commitContext: 'Unit'
        ]
        jobParams.env = [
            UNIT_TESTS: true,
            INTEGRATION_TESTS: false
        ]
    }

    // Integration tests
    setupPrJob(jobFolder) { jobParams ->
        jobParams.job.description = "Run integration tests from ${jobParams.job.name} repository"
        jobParams.job.name += '.integration-tests'
        jobParams.pr = [
            trigger_phrase : '(.*[j|J]enkins,? run [I|i]ntegration[ tests]?.*)|(.*Unit.*successful.*)',
            trigger_phrase_only: true,
            commitContext: 'Integration'
        ]
        jobParams.env = [
            UNIT_TESTS: false,
            INTEGRATION_TESTS: true
        ]
    }

    // Optaplanner tests
    setupPrJob(jobFolder) { jobParams ->
        jobParams.job.description = "Run tests of Optaplanner due to changes in ${jobParams.job.name} repository"
        jobParams.git.repo_url = 'https://github.com/${ghprbPullAuthorLogin}/optaplanner/'
        jobParams.job.name += '.optaplanner-tests'
        jobParams.pr = [
            checkout_branch: ':(origin/${ghprbSourceBranch}|origin/${ghprbTargetBranch})',
            trigger_phrase : '(.*[j|J]enkins,? run [O|o]ptaplanner[ tests]?.*)|(.*Integration.*successful.*)',
            trigger_phrase_only: true,
            commitContext: 'Optaplanner'
        ]
        jobParams.env = [
            UNIT_TESTS: true,
            INTEGRATION_TESTS: false
        ]
    }

    // Kogito-apps tests
    setupPrJob(jobFolder) { jobParams ->
        jobParams.job.description = "Run tests of Kogito-apps due to changes in ${jobParams.job.name} repository"
        jobParams.git.repo_url = 'https://github.com/${ghprbPullAuthorLogin}/kogito-apps/'
        jobParams.job.name += '.kogito-apps-tests'
        jobParams.pr = [
            checkout_branch: ':(origin/${ghprbSourceBranch}|origin/${ghprbTargetBranch})',
            trigger_phrase : '(.*[j|J]enkins,? run [A|a]pps[ tests]?.*)|(.*Optaplanner.*successful.*)',
            trigger_phrase_only: true,
            commitContext: 'Kogito-apps'
        ]
        jobParams.env = [
            UNIT_TESTS: true,
            INTEGRATION_TESTS: true
        ]
    }

    // Kogito-examples tests
    setupPrJob(jobFolder) { jobParams ->
        jobParams.job.description = "Run tests of Kogito-examples due to changes in ${jobParams.job.name} repository"
        jobParams.git.repo_url = 'https://github.com/${ghprbPullAuthorLogin}/kogito-examples/'
        jobParams.job.name += '.kogito-examples-tests'
        jobParams.pr = [
            checkout_branch: ':(origin/${ghprbSourceBranch}|origin/${ghprbTargetBranch})',
            trigger_phrase : '(.*[j|J]enkins,? run [E|e]xamples[ tests]?.*)|(.*Optaplanner.*successful.*)',
            trigger_phrase_only: true,
            commitContext: 'Kogito-examples'
        ]
        jobParams.env = [
            UNIT_TESTS: true,
            INTEGRATION_TESTS: true
        ]
    }
}

void setupPrJob(String jobFolder, Closure updateJobParams) {
    def jobParams = getDefaultJobParams()
    jobParams.job.folder = jobFolder
    updateJobParams(jobParams)
    KogitoJobTemplate.createPRJob(this, jobParams)
}

void setupQuarkusLTSPrJob(String jobFolder) {
    def jobParams = getDefaultJobParams()
    jobParams.job.folder = jobFolder
    KogitoJobTemplate.createQuarkusLTSPRJob(this, jobParams)
}

void setupNativePrJob(String jobFolder) {
    def jobParams = getDefaultJobParams()
    jobParams.job.folder = jobFolder
    KogitoJobTemplate.createNativePRJob(this, jobParams)
}

void setupDroolsJob(String jobFolder) {
    def jobParams = getJobParams('kogito-drools-snapshot', jobFolder, 'Jenkinsfile.drools', 'Kogito Runtimes Drools Snapshot')
    jobParams.triggers = [ cron : 'H 2 * * *' ]
    KogitoJobTemplate.createPipelineJob(this, jobParams).with {
        parameters {
            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')
            stringParam('GIT_AUTHOR', "${GIT_AUTHOR_NAME}", 'Set the Git author to checkout')
        }
        environmentVariables {
            env('JENKINS_EMAIL_CREDS_ID', "${JENKINS_EMAIL_CREDS_ID}")
        }
    }
}

void setupQuarkusJob(String jobFolder, String quarkusBranch) {
    def jobParams = getJobParams("kogito-quarkus-${quarkusBranch}", jobFolder, 'Jenkinsfile.quarkus', 'Kogito Runtimes Quarkus Snapshot')
    jobParams.triggers = [ cron : 'H 4 * * *' ]
    KogitoJobTemplate.createPipelineJob(this, jobParams).with {
        parameters {
            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')
            stringParam('GIT_AUTHOR', "${GIT_AUTHOR_NAME}", 'Set the Git author to checkout')
        }
        environmentVariables {
            env('JENKINS_EMAIL_CREDS_ID', "${JENKINS_EMAIL_CREDS_ID}")
            env('QUARKUS_BRANCH', quarkusBranch)
        }
    }
}

void setupSonarCloudJob(String jobFolder) {
    def jobParams = getJobParams('kogito-runtimes-sonarcloud', jobFolder, 'Jenkinsfile.sonarcloud', 'Kogito Runtimes Daily Sonar')
    jobParams.triggers = [ cron : 'H 20 * * 1-5' ]
    KogitoJobTemplate.createPipelineJob(this, jobParams).with {
        parameters {
            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')
            stringParam('GIT_AUTHOR', "${GIT_AUTHOR_NAME}", 'Set the Git author to checkout')
        }
        environmentVariables {
            env('JENKINS_EMAIL_CREDS_ID', "${JENKINS_EMAIL_CREDS_ID}")
        }
    }
}

void setupNativeJob(String jobFolder) {
    def jobParams = getJobParams('kogito-native', jobFolder, 'Jenkinsfile.native', 'Kogito Runtimes Native Testing')
    jobParams.triggers = [ cron : 'H 6 * * *' ]
    KogitoJobTemplate.createPipelineJob(this, jobParams).with {
        parameters {
            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')
            stringParam('GIT_AUTHOR', "${GIT_AUTHOR_NAME}", 'Set the Git author to checkout')
        }
        environmentVariables {
            env('JENKINS_EMAIL_CREDS_ID', "${JENKINS_EMAIL_CREDS_ID}")
        }
    }
}

void setupDeployJob(String jobFolder, KogitoJobType jobType) {
    def jobParams = getJobParams('kogito-runtimes-deploy', jobFolder, 'Jenkinsfile.deploy', 'Kogito Runtimes Deploy')
    if (jobType == KogitoJobType.PR) {
        jobParams.git.branch = '${BUILD_BRANCH_NAME}'
        jobParams.git.author = '${GIT_AUTHOR}'
        jobParams.git.project_url = Utils.createProjectUrl("${GIT_AUTHOR_NAME}", jobParams.git.repository)
    }
    KogitoJobTemplate.createPipelineJob(this, jobParams).with {
        parameters {
            stringParam('DISPLAY_NAME', '', 'Setup a specific build display name')

            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')
            if (jobType == KogitoJobType.PR) {
                // author can be changed as param only for PR behavior, due to source branch/target, else it is considered as an env
                stringParam('GIT_AUTHOR', "${GIT_AUTHOR_NAME}", 'Set the Git author to checkout')
            }

            // Build&test information
            booleanParam('SKIP_TESTS', false, 'Skip tests')

            // Release information
            booleanParam('CREATE_PR', false, 'Should we create a PR with the changes ?')
            stringParam('PROJECT_VERSION', '', 'Set the project version')
        }

        environmentVariables {
            env('REPO_NAME', 'kogito-runtimes')

            env('RELEASE', jobType == KogitoJobType.RELEASE)
            env('JENKINS_EMAIL_CREDS_ID', "${JENKINS_EMAIL_CREDS_ID}")
            env('MAVEN_SETTINGS_CONFIG_FILE_ID', "${MAVEN_SETTINGS_FILE_ID}")

            if (jobType == KogitoJobType.PR) {
                env('MAVEN_DEPENDENCIES_REPOSITORY', "${MAVEN_PR_CHECKS_REPOSITORY_URL}")
                env('MAVEN_DEPLOY_REPOSITORY', "${MAVEN_PR_CHECKS_REPOSITORY_URL}")
                env('MAVEN_REPO_CREDS_ID', "${MAVEN_PR_CHECKS_REPOSITORY_CREDS_ID}")
            } else {
                env('GIT_AUTHOR', "${GIT_AUTHOR_NAME}")

                env('AUTHOR_CREDS_ID', "${GIT_AUTHOR_CREDENTIALS_ID}")
                env('GITHUB_TOKEN_CREDS_ID', "${GIT_AUTHOR_TOKEN_CREDENTIALS_ID}")
                env('GIT_AUTHOR_BOT', "${GIT_BOT_AUTHOR_NAME}")
                env('BOT_CREDENTIALS_ID', "${GIT_BOT_AUTHOR_CREDENTIALS_ID}")

                env('MAVEN_DEPENDENCIES_REPOSITORY', "${MAVEN_ARTIFACTS_REPOSITORY}")
                env('MAVEN_DEPLOY_REPOSITORY', "${MAVEN_ARTIFACTS_REPOSITORY}")
                if (jobType == KogitoJobType.RELEASE) {
                    env('NEXUS_RELEASE_URL', "${MAVEN_NEXUS_RELEASE_URL}")
                    env('NEXUS_RELEASE_REPOSITORY_ID', "${MAVEN_NEXUS_RELEASE_REPOSITORY}")
                    env('NEXUS_STAGING_PROFILE_ID', "${MAVEN_NEXUS_STAGING_PROFILE_ID}")
                    env('NEXUS_BUILD_PROMOTION_PROFILE_ID', "${MAVEN_NEXUS_BUILD_PROMOTION_PROFILE_ID}")
                }
            }
        }
    }
}

void setupPromoteJob(String jobFolder, KogitoJobType jobType) {
    KogitoJobTemplate.createPipelineJob(this, getJobParams('kogito-runtimes-promote', jobFolder, 'Jenkinsfile.promote', 'Kogito Runtimes Promote')).with {
        parameters {
            stringParam('DISPLAY_NAME', '', 'Setup a specific build display name')

            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')

            // Deploy job url to retrieve deployment.properties
            stringParam('DEPLOY_BUILD_URL', '', 'URL to jenkins deploy build to retrieve the `deployment.properties` file.')

            // Release information which can override `deployment.properties`
            stringParam('PROJECT_VERSION', '', 'Override `deployment.properties`. Give the project version.')

            stringParam('GIT_TAG', '', 'Git tag to set, if different from PROJECT_VERSION')
        }

        environmentVariables {
            env('REPO_NAME', 'kogito-runtimes')
            env('PROPERTIES_FILE_NAME', 'deployment.properties')

            env('RELEASE', jobType == KogitoJobType.RELEASE)
            env('JENKINS_EMAIL_CREDS_ID', "${JENKINS_EMAIL_CREDS_ID}")

            env('GIT_AUTHOR', "${GIT_AUTHOR_NAME}")

            env('AUTHOR_CREDS_ID', "${GIT_AUTHOR_CREDENTIALS_ID}")
            env('GITHUB_TOKEN_CREDS_ID', "${GIT_AUTHOR_TOKEN_CREDENTIALS_ID}")
            env('GIT_AUTHOR_BOT', "${GIT_BOT_AUTHOR_NAME}")
            env('BOT_CREDENTIALS_ID', "${GIT_BOT_AUTHOR_CREDENTIALS_ID}")

            env('MAVEN_SETTINGS_CONFIG_FILE_ID', "${MAVEN_SETTINGS_FILE_ID}")
            env('MAVEN_DEPENDENCIES_REPOSITORY', "${MAVEN_ARTIFACTS_REPOSITORY}")
            env('MAVEN_DEPLOY_REPOSITORY', "${MAVEN_ARTIFACTS_REPOSITORY}")
        }
    }
}
