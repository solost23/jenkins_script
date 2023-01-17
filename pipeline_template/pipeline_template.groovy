#!/usr/bin/env groovy

// 注意: 有些项目名ps查询会返回多个东西，此时采取一个中间变量替换project_name即可，这里采用project_name_local作为部署实际项目名称
// 运行项目时默认会读取从git上拉下来的配置，如果想让项目读取自己写的配置，可以采取的一个方式是指定项目工作目录，然后在项目里拼接配置文件地址，运行项目时传送项目目录参数即可
def git_address = "git@github.com:solost23/twitta.git"
def git_branch = "develop"

def project_name = "twitta"

def project_name_local = "twitta"
def project_path = "/Users/ty/${project_name_local}"

// 发送飞书报告脚本路径
def report_template_path = "/Users/ty/jenkins_script/lark_message/lark_message"
def build_result_path = "/Users/ty/jenkins_script/lark_message/complate_or_fail"

def app = "后端"

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
                /Users/ty/go/go1.19.4/bin/go build -o ${project_name_local} ./cmd/main.go
                rm -rf ./configs
                cp -rf ./ ${project_path}/
                """
            }
        }

        stage('4.部署程序'){
            steps {
                script{
                    PROCESS_ID = sh(script: "ps -ef|grep ${project_name_local}|grep -v grep|awk \'{print \$2}\'", returnStdout: true).trim()
                    echo "${PROCESS_ID}"

                    if (PROCESS_ID != "") {
                        sh """
                        echo "Kill process: ${PROCESS_ID}"
                        kill -9 ${PROCESS_ID}
                        """
                    }
                }

                sh """
                JENKINS_NODE_COOKIE=dontKillMe nohup ${project_path}/${project_name_local} -d ${project_path} &
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
                ${report_template_path}/lark_message -filename ${report_template_path}/message_template.json -number $BUILD_NUMBER -url $JOB_URL -name $JOB_NAME
                """
                sh """
                ${build_result_path}/complate_or_fail -filename ${build_result_path}/complate_or_fail_template.json -app ${app} -name $JOB_NAME -result 成功
                """
            }

        }
        failure{
            script{
                sh """
                ${build_result_path}/complate_or_fail -filename ${build_result_path}/complate_or_fail_template.json -app ${app} -name $JOB_NAME -result 失败
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