import argparse
import os, atexit
import textwrap


import signal
import random
import time
from enum import Enum

from collections import defaultdict, OrderedDict


# def parse_receiver_file(file):
#     with open(file) as f:
#         lines = f.readlines()
#         print("Number of packets received:", len(lines))

def parse_sender_file(file):
     with open(file) as f:
        lines = f.readlines()
        print(f"{file} sent {len(lines)} packets")

def parse_output_files(sender_files, receiver_file):
    messages = []
    with open(receiver_file) as f:
        for line in f.readlines():
            m = line[2:]
            messages.append(m)
    print("Number of messages:", len(messages))
    print("Number of duplicated delivered messages:", len(messages) - len(set(messages)))
    for sender_file in sender_files:
        end_idx = sender_file.index('.output')
        start_idx = end_idx - 2
        sender_id = int(sender_file[start_idx:end_idx])
        with open(sender_file) as f:
            for line in f.readlines():
                m = f"{sender_id} {line[2:]}"
                if m in messages:
                    messages.remove(m)
    print("Remaining number of messages:", len(messages))
    if len(messages):
        missing_messages = {}
        for m in messages:
            sender_id = int(m.split(" ")[0])
            seq_nb = int(m.plist(" ")[1])
            if sender_id not in missing_messages:
                missing_messages[sender_id] = []
            missing_messages[sender_id].append(seq_nb)
        for sender_id in missing_messages.keys():
            print("Number of duplicated sent messages:", len(missing_messages[sender_id]) - len(set(missing_messages[sender_id])))
            print(f"{sender_id} has {len(missing_messages[sender_id])} packets not delivered by the receiver")
                


if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument(
        "--proc_num",
        required=True,
        type=int,
        dest="proc_num",
        help="Total number of processes",
    )

    parser.add_argument(
        "--receiver_id",
        required=True,
        type=int,
        dest="receiver_id",
        help="Receiver ID",
    )

    parser.add_argument('--log_folder', required=True, dest="log_folder",
        help="log_folder",)

    results = parser.parse_args()

    sender_files = []
    for file in os.listdir(results.log_folder):
        if file == f"proc{results.receiver_id:02d}.output":
            receiver_file = os.path.join(results.log_folder, file)
            # parse_receiver_file(os.path.join(results.log_folder, file))
        elif file.endswith(".output"):
            parse_sender_file(os.path.join(results.log_folder, file))
            sender_files.append(os.path.join(results.log_folder, file))

    parse_output_files(sorted(sender_files), receiver_file)
