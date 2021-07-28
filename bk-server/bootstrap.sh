#!/bin/sh
## java env
export JAVA_HOME=/opt/jdk1.8.0_11
export JRE_HOME=$JAVA_HOME/jre
 
API_NAME=$1
BASE_PATH=$(cd `dirname $0`; pwd)
JAR_NAME=$BASE_PATH/$API_NAME\.jar
#PIDFILE  代表是PID文件
PIDFILE=$API_NAME\.pid
 
#使用说明，用来提示输入参数
usage() {
    echo "Usage: sh 执行脚本.sh [APP_NAME] [start|stop|restart|status]"
    exit 1
}
 
#检查程序是否在运行
is_exist(){
  if [ -f $PIDFILE ]; then
    echo "$PIDFILE file exists...." 
    pid=$(cat $PIDFILE)          # 将PID从文件中读取，并作为一个变量
	#如果不存在返回1，存在返回0     
	if [ -z "${pid}" ]; then
	   return 1
	else
	   return 0
	fi   
  else	   
    return 1
  fi
}
 
#启动方法
start(){
  is_exist
  if [ $? -eq "0" ]; then 
    echo ">>> ${JAR_NAME} is already running PID=${pid} <<<" 
  else 
    nohup $JRE_HOME/bin/java -Xms256m -Xmx512m -Xmn30m -jar $JAR_NAME >/dev/null 2>&1 &
    echo $! > $PIDFILE
    echo ">>> start $JAR_NAME successed PID=$! <<<" 
   fi
  }
 
#停止方法
stop(){
  is_exist
  if [ $? -eq "0" ]; then
	#echo "$pidf"  
	echo ">>> PID = $pid begin kill $pid <<<"
	kill $pid
	rm -rf $PIDFILE
	sleep 2 
	PID_EXIST=$(ps aux | awk '{print $2}'| grep -w $pid)
	if [ $PID_EXIST ];then
	  echo ">>> PID = $pid begin kill -9 $pid  <<<"
      kill -9  $pid
      sleep 2
	fi  
	echo ">>> $JAR_NAME process stopped <<<"  
  else
	echo ">>> ${JAR_NAME} is not running <<<"
  fi 
}
 
#输出运行状态
status(){
  is_exist
  if [ $? -eq "0" ]; then
    echo ">>> ${JAR_NAME} is running PID is ${pid} <<<"
  else
    echo ">>> ${JAR_NAME} is not running <<<"
  fi
}
 
#重启
restart(){
  stop
  start
}
 
#根据输入参数，选择执行对应方法，不输入则执行使用说明
case "$2" in
  "start")
    start
    ;;
  "stop")
    stop
    ;;
  "status")
    status
    ;;
  "restart")
    restart
    ;;
  *)
    usage
    ;;
esac
exit 0
