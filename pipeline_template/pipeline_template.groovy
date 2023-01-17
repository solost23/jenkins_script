#!/usr/bin/env groovy

def git_address = "git@github.com:solost23/jenkins_build_test.git"
def git_branch = "develop"


pipeline {
    agent{
        label "master"
    }

    stages {
        stage('1.拉取代码'){
            steps {
             //checkout([$class: 'GitSCM', branches: [[name: '${Branch}']], userRemoteConfigs: [[credentialsId: "${git_auth}", url: "${git_address}"]]])
                 git  branch: "${git_branch}", url: "${git_address}"
                     // }
            }
        }


        stage('3.编译程序'){
            steps {
                sh """
                    export GOPROXY=https://goproxy.cn/
                    /Users/ty/go/go1.19.4/bin/go version
                    /Users/ty/go/go1.19.4/bin/go build -o jenkins_build_test main.go
            cp jenkins_build_test /Users/ty/jenkins_build_test
                """
            }
        }

        stage('4.部署程序'){
            steps {
                script{
                    PROCESS_ID = sh(script: "ps -ef|grep jenkins_build_test|grep -v grep|awk \'{print \$2}\'", returnStdout: true).trim()
                    echo "${PROCESS_ID}"

                    if (PROCESS_ID != "") {
                        sh """
                             echo "Kill process: ${PROCESS_ID}"
                             kill -9 ${PROCESS_ID}
                            """
                    }
                }

                sh """
                JENKINS_NODE_COOKIE=dontKillMe nohup /Users/ty/jenkins_build_test/jenkins_build_test &
                """
            }
        }
    }

    post {
        always{
            script{
                println("流水线结束后，经常做的事情")
            }
        }

        success{
            script{
                sh """
                /Users/ty/jenkins_script/lark_message/lark_message/lark_message -filename /Users/ty/jenkins_script/lark_message/lark_message/message_template.json -number $BUILD_NUMBER -url $JOB_URL -name $JOB_NAME
                """
                sh """
                /Users/ty/jenkins_script/lark_message/complate_or_fail/complate_or_fail -filename /Users/ty/jenkins_script/lark_message/complate_or_fail/complate_or_fail_template.json -app 后端 -name $JOB_NAME -result 成功
                """
            }

        }
        failure{
            script{
                sh """
                /Users/ty/jenkins_script/lark_message/complate_or_fail/complate_or_fail -filename /Users/ty/jenkins_script/lark_message/complate_or_fail/complate_or_fail_template.json -app 后端 -name $JOB_NAME -result 失败
                """
            }
        }

        aborted{
            script{
                println("流水线取消后，要做的事情")
            }

        }
    }
}