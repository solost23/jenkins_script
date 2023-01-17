package main

import (
	"flag"
	"fmt"
	"net/http"
	"os"
	"strings"
	"time"
)

/*发送项目飞书构建报告*/

func main() {
	var jobUrl, jobName, buildNumber, fileName string
	flag.StringVar(&jobUrl, "url", "", "项目url")
	flag.StringVar(&jobName, "name", "", "项目名称")
	flag.StringVar(&buildNumber, "number", "1", "构建次数")
	flag.StringVar(&fileName, "filename", "./message_template.json", "模板文件名")
	flag.Parse()

	data, err := buildLarkMessage(fileName, time.Now(), jobUrl, jobName, buildNumber)
	if err != nil {
		fmt.Println(err)
		os.Exit(-1)
	}
	url := "https://open.feishu.cn/open-apis/bot/v2/hook/979dc01e-3506-4c8c-8f65-bda4c0116625"
	resp, err := http.Post(url, "application/json", strings.NewReader(data))
	if err != nil {
		fmt.Println(err)
		os.Exit(-1)
	}
	if resp.StatusCode > 299 {
		fmt.Println("错误码: ", resp.StatusCode)
		os.Exit(-1)
	}
}

func buildLarkMessage(fileName string, currentTime time.Time, jobUrl, jobName, buildNumber string) (string, error) {
	b, err := os.ReadFile(fileName)
	if err != nil {
		return "", err
	}
	return fmt.Sprintf(string(b), jobName, buildNumber, currentTime.Format("2006/01/02 15:04:05"), jobUrl, jobName), nil
}
