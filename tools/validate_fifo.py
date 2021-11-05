#!/usr/bin/env python3

import argparse
import os, atexit
import textwrap


import signal
import random
import time
from enum import Enum

from collections import defaultdict, OrderedDict

def check_positive(value):
    ivalue = int(value)
    if ivalue <= 0:
        raise argparse.ArgumentTypeError("{} is an invalid positive int value".format(value))
    return ivalue


def checkProcess(filePath):
    i = 1
    nextMessage = defaultdict(lambda : 1)
    filename = os.path.basename(filePath)

    with open(filePath) as f:
        for lineNumber, line in enumerate(f):
            tokens = line.split()

            # Check broadcast
            if tokens[0] == 'b':
                msg = int(tokens[1])
                if msg != i:
                    print("File {}, Line {}: Messages broadcast out of order. Expected message {} but broadcast message {}".format(filename, lineNumber, i, msg))
                    return False
                i += 1

            # Check delivery
            if tokens[0] == 'd':
                sender = int(tokens[1])
                msg = int(tokens[2])
                if msg != nextMessage[sender]:
                    print("File {}, Line {}: Message delivered out of order. Expected message {}, but delivered message {}".format(filename, lineNumber, nextMessage[sender], msg))
                    return False
                else:
                    nextMessage[sender] = msg + 1

    return True

def checkStderr(stderr):
    with open(stderr) as f:
        return len(f.readlines()) == 0 # True iff stderr file is empty

def check_stdout(stdout):
    with open(stdout) as f:
        lines = f.readlines()
        start_line = [line for line in lines if "start" in line]
        if len(start_line) != 1:
            print("Multiple (or None) start printed")
        else:
            start = float(start_line[0].split(" ")[1])

        end_line = [line for line in lines if "end" in line]
        if len(end_line) != 1:
            print("Multiple (or None) end printed")
        else:
            end = float(end_line[0].split(" ")[1])
        return start, end


if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument(
        "--proc_num",
        required=True,
        type=check_positive,
        dest="proc_num",
        help="Total number of processes",
    )
    parser.add_argument(
        "--log_folder",
        required=True,
        dest="log_folder",
        help="log_folder",
    )

    results = parser.parse_args()
    files = os.listdir(results.log_folder)
    output_files = sorted([f for f in files if f.endswith('.output')])
    stdout_files = sorted([f for f in files if f.endswith('.stdout')])
    stderr_files = sorted([f for f in files if f.endswith('.stderr')])

    all_ok = True
    earliest_process = -1
    latest_process = -1
    earliest_start = float('inf')
    latest_start = float('-inf')
    earliest_end = float('inf')
    latest_end = float('-inf')
    for i in range(results.proc_num):
        o = output_files[i]
        print("Checking {}".format(o))
        if not checkProcess(os.path.join(results.log_folder,o)):
            all_ok = False
            print("Validation failed!")
        if not checkStderr(os.path.join(results.log_folder, stderr_files[i])):
            print("Stderr not empty!")

        start, end = check_stdout(os.path.join(results.log_folder, stdout_files[i]))
        if start < earliest_start:
            earliest_start = start
            earliest_process = stdout_files[i]
        if start > latest_start:
            latest_start = start

        if end < earliest_end:
            earliest_end = end
        if end > latest_end:
            latest_end = end
            latest_process = stdout_files[i]

    
    print("All outputs are valid" if all_ok else "Some outputs are not valid")
    # print("First process to start:", earliest_process)
    # print("Last process to end:", latest_process)
    print("Time between first start and last start:", latest_start - earliest_start, "ms")
    print("Time between last start and last end:", latest_end - latest_start, "ms")
    print("Time between last start and first end:", earliest_end - latest_start, "ms")
    print("Overall time:", latest_end - earliest_start, "ms")

