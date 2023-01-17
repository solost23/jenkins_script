package main

import (
	"flag"
	"fmt"
	"net/http"
	"os"
	"strings"
)

func main() {
	var fileName, app, jobName, result string
	flag.StringVar(&fileName, "filename", "./complate_or_fail_template.json", "模板文件名")
	flag.StringVar(&app, "app", "后端", "前端OR后端")
	flag.StringVar(&jobName, "name", "", "项目名称")
	flag.StringVar(&result, "result", "成功", "构建结果")
	flag.Parse()

	data, err := buildLarkMessage(fileName, app, jobName, result)
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

func buildLarkMessage(fileName, app, jobName, result string) (string, error) {
	b, err := os.ReadFile(fileName)
	if err != nil {
		return "", err
	}
	return fmt.Sprintf(string(b), app, jobName, result), nil
}
