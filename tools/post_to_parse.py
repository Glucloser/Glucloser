#!/usr/bin/python

import csv
from datetime import datetime, timedelta
import json
import re
import requests
import sys

def create_INT(data):
    return int(data)

def create_FLOAT(data):
    return float(data)

def _create_DATEOBJ(data, tmz_delta, format):
    date = datetime.strptime(data, format)
    if tmz_delta:
        date += tmz_delta
    return date

def dateToStr(date):
    # Since datetime doesn't understand parsing timezones
    return date.strftime("%Y-%m-%dT%H:%M:%S") + "-04:00"

def create_DATE(data, tmz_delta):
    date = _create_DATEOBJ(data, tmz_delta, "%m/%d/%y")
    return dateToStr(date)

def create_TIME(data, tmz_delta):
    date = _create_DATEOBJ(data, tmz_delta, "%H:%M:%S")
    time_struct = date.timetuple()
    hour = time_struct[3] * 60 * 60
    min =  time_struct[4] * 60
    sec =  time_struct[5]
    return hour + min + sec

def create_TIMESTAMP(data, tmz_delta):
    date = _create_DATEOBJ(data, tmz_delta, "%m/%d/%y %H:%M:%S")
    return dateToStr(date)

def create_STRING(data):
    return str(data)

not_allowed_re = re.compile("[^a-zA-Z0-9]")
def massage_column_header(header):
    return re.sub(not_allowed_re, "_", header)

def hilite(string, status, bold):
    attr = []
    if status >= 200 and status < 400:
        # green
        attr.append('32')
    elif status >= 400 and status < 500:
        # red
        attr.append('31')
    if bold:
        attr.append('1')
    return '\x1b[%sm%s\x1b[0m' % (';'.join(attr), string)

API_KEY_HEADER = "X-Cairo-API-Key"
APP_ID_HEADER = "X-Cairo-Application-ID"

API_KEY = "GODRG097oZwx2/3IvTX57NjkRtbXfrafY1uHz+ZC9P61XhVhR/m3RWpeYNaDfHR2joUqcHOjkETOfIeoy1HyBcj/OnhvK/7DUShI9FuuFt7g16CWo9NUu9LzJ1GZVFZLqnxdr5/MX1Vyn4mdfqGZ+dWG5yk0R+PkDaWA18Yei26q24RHraShip3lSNa6Bxs3VgD+ksrQqigYu93V6QzjEy3Etj/NtFcegDahJjfwd6mhvtZBcNRLs+aw+Ub6Tf6/XvJcvNphOgmKE672NEX4/VeUdNkluLvN8udfp/VWERXlSPDpg/bnpINGCyW6eWfbORUCznZ2iG1REejf+ZNUDQ=="
APP_ID = "glucloser"

URL = "https://cairo.glucloser.com:443/pump/data/upload"

HEADERS = {API_KEY_HEADER: API_KEY, APP_ID_HEADER: APP_ID, "Content-Type": "application/json"}

# Data types
INT = 0
FLOAT = 1
DATE = 2
TIME = 3
TIMESTAMP = 4
STRING = 5

# Export format v1.0.1
column_types = [INT, DATE, STRING, TIMESTAMP, TIMESTAMP, INT, STRING, FLOAT, STRING, STRING, STRING, FLOAT, FLOAT, TIME, STRING, FLOAT, STRING, STRING, FLOAT, INT, INT, INT, INT, INT, INT, FLOAT, FLOAT, FLOAT, STRING, INT, INT, FLOAT, FLOAT, STRING, STRING, INT, INT, INT, STRING]

skip_info_rows_count = 11

# Don't have any way to get the GMT offset of the device
# TODO(nl) EST/EDT
tz_delta = timedelta(hours=4)

csv_file = sys.argv[1]

if not csv_file:
    print "Takes a path to the csv to upload", "No data uploaded"
    exit(-1)

isTTY = sys.stdout.isatty()

csv_reader = csv.reader(open(csv_file, 'rb'))

while skip_info_rows_count:
    csv_reader.next()
    skip_info_rows_count -= 1

column_headers = csv_reader.next()
if len(column_headers) != len(column_types):
    print "Columnt data types length does not match headers length"
    print "Found %d columns, expected %d" % (len(column_headers), len(column_types))
    print "No data uploaded"
    exit(-1)

for row in csv_reader:
    payload = {}

    for type, data, column_header in zip(column_types, row, column_headers):
        if data == "":
            continue
        value = {}
        if type == INT:
            value = create_INT(data)
        elif type == FLOAT:
            value = create_FLOAT(data)
        elif type == DATE:
            value = create_DATE(data, tz_delta)
        elif type == TIME:
            value = data #create_TIME(data, tz_delta)
        elif type == TIMESTAMP:
            value = create_TIMESTAMP(data, tz_delta)
        elif type == STRING:
            value = create_STRING(data)
        else:
            print "Unknown column type '", type, "'"
            exit(-1)

        payload[massage_column_header(column_header)] = value

    if not payload:
        print "Empty payload, skipping"
        continue

    print "Uploading ", payload

    response = requests.put(URL, data=json.dumps(payload), headers=HEADERS)

    if isTTY and response.status_code >= 400 and response.status_code < 500:
        print hilite("Response code " + str(response.status_code) + " => " + response.text, response.status_code, True)
        exit()
    else:
        print hilite("Response code " + str(response.status_code) + " => " + response.text, response.status_code, False)
