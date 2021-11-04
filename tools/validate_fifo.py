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
    all_ok = True
    for o in sorted([f for f in os.listdir(results.log_folder) if f.endswith('.output')])[:results.proc_num]:
        print("Checking {}".format(o))
        if checkProcess(os.path.join(results.log_folder,o)):
            print("Validation OK")
        else:
            all_ok = False
            print("Validation failed!")
    if all_ok:
        print("All outputs are valid")
    else:
        print("Some outputs are not valid")


