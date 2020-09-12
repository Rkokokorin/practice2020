pipeline {
  agent any
  environment{
    JAVA_HOME = '/usr/lib/jvm/java-11-openjdk-amd64/'
  }

  stages {
    stage('Build Liferay portal') {
      steps {
        gitlabCommitStatus(name: 'Build Liferay portal') {
          dir("${env.WORKSPACE}/liferay/"){
            sh 'mvn -B -DskipTests clean package'
          }
        }
        gitlabCommitStatus(name: 'Check style in Liferay portal') {
          dir("${env.WORKSPACE}/liferay/") {
            sh 'mvn -B --fail-at-end -Dcheckstyle.violationSeverity=warning checkstyle:check'
          }
        }
      }
    }

    stage('Build REST services') {
      steps {
        gitlabCommitStatus(name: 'Build REST services') {
          dir("${env.WORKSPACE}/rest/"){
            sh 'mvn -B -DskipTests clean package'
          }
        }
      }
    }

    stage('Test REST services') {
      steps {
        gitlabCommitStatus(name: 'Test REST services') {
          dir("${env.workspace}/rest/"){
            sh 'mvn -B -DskipTests clean package'
            sh 'mvn test'
          }
        }
      }
      post {
        always {
          dir("${env.workspace}/rest/"){
            junit 'target/surefire-reports/*.xml'
          }
        }
      }
    }

  }

  post {
    always {
      emailext(body: '$DEFAULT_CONTENT',
        recipientProviders: [[$class: 'DevelopersRecipientProvider'],
          [$class: 'RequesterRecipientProvider']],
        subject: '$DEFAULT_SUBJECT')
    }
  }
}
