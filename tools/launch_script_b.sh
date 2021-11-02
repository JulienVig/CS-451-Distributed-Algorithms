#!/bin/bash
# 1 : nbr process, 2 : nbr message, 3 : sleep timing

logfolder='./log/'
> host
> config
echo $2 > config
for ((i=1;i<=$1;i++))
do
	echo $i localhost $((11000 + $i)) >> host
	> ${logfolder}out$i.txt #Deleting all previous output file
	echo ======file $i begining====== > ${logfolder}out$i.txt 
done

for ((i=1;i<=$1;i++))
do
	#Launching the processes
	./run.sh --id $i --hosts host --output ${logfolder}out$i.txt config > ${logfolder}log$i.txt 2> ${logfolder}errors$i.txt < /dev/null &
done
sleep $3

killall java
sleep 1
for ((i=1;i<=$1;i++))
do
	cat ${logfolder}out$i.txt
done

for ((i=1;i<=$1;i++))
do
	echo file number $i lines :
	wc -l ${logfolder}out$i.txt
done