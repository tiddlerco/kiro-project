#!/usr/bin/env bash
# 临时启动脚本：固定使用本机 JDK 8（Corretto 1.8.0_432）启动 Spring Boot 应用做上下文加载验证。
# 用途：仅用于本次启动验证，验证完成后由执行流程删除本脚本。
export JAVA_HOME=/Users/yuke/Library/Java/JavaVirtualMachines/corretto-1.8.0_432/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
exec mvn -B spring-boot:run
