### script to build and run the project

import sys 
import os 
import subprocess
import time
import random
import string

def get_random_string(length):
    # choose from all lowercase letter
    letters = string.ascii_lowercase
    result_str = ''.join(random.choice(letters) for i in range(length))
    return result_str


## GETTING PATHS ================================================================================

shebang = "/bin/sh"

subprocess.call([shebang, "./build.sh"])
## 2 - current working directory
PATH = os.path.join(os.getcwd(), "settings")

## 1 - get the number of sender process you want
process_nb = int(sys.argv[1]) if (len(sys.argv) > 1) else 1
base_pid = 1
base_port = 10010


## creation of the path to output
config_path = os.path.join(PATH, "config.txt")
hosts_path = os.path.join(PATH, "hosts.txt")
output_spec_path = "out/ouput_p{}.log"


## BUILDING FILE ================================================================================

## CONFIG FILE
m = int(sys.argv[2]) if(len(sys.argv) > 2) else 10

if(not os.path.exists(PATH)):
    os.makedirs(PATH)

if(not os.path.exists(os.path.join(PATH, "out"))):
    os.makedirs(os.path.join(PATH, "out"))

with open("./settings/config.txt", 'w') as config:
    config.write(str(m)) ## we write the number of messages to send/ sender process
    config.write(", ")
    config.write(str(base_pid)) ## we write which process is suppose to send
    config.write(", ")
    config.write(get_random_string(10)) ## we write a random payload

print("> CONFIG FILE CREATED")
## HOSTS FILE
with open("./settings/hosts.txt", 'w') as hosts:
    hosts.write(f"1/localhost/{base_port}\n")
    for i in range(1, process_nb + 1):
        hosts.write(f"{i + 1}/localhost/{base_port + i}\n")
        
print("> HOST FILE CREATED")

## LAUNCHING THE PROCESSES ========================================================================

BASE_COMMAND = shebang + ">>>./run.sh>>>--id>>>{}>>>--hosts>>>" + hosts_path.replace("\\", "/") +">>>--output>>>{}>>>" + config_path.replace("\\", "/")

def build_command_line(pid):
    return BASE_COMMAND.format(pid, os.path.join(PATH, output_spec_path.format(pid)).replace("\\", "/")).split(">>>")


processes = []
# # spawning the receiver process :
processes.append(subprocess.Popen(build_command_line(base_pid), shell=True))

print("> receiver process launched")

for i in range(1, process_nb + 1):
    cur_pid = base_pid + i 
    ## launch reciever process
    print(build_command_line(cur_pid))
    processes.append(subprocess.Popen(build_command_line(cur_pid), shell=True))
    print("> sender process launched")



time.sleep(1000)

## kill them all
[process.terminate for process in processes]







