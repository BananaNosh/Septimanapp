import os

from docx.api import Document
import argparse
import datetime
import re
import json

LATEST_END_HOUR = 23
END_TIME_KEY = "mEndTime"


def read_args():
    opts = argparse.ArgumentParser(description='Create a json file which can be loaded in Septimanapp '
                                               'from a word file containing the Horarium')
    opts.add_argument('wordfile', metavar='WORD_FILE', type=argparse.FileType("r"),
                      help='path to word file with the Horarium')
    opts.add_argument('date', metavar='FIRST_DAY', type=str,
                      help='First day of the septimana (DD.MM.YY)')
    args = opts.parse_args()
    return args


def create_event(id, text, date, start_hour, start_minute, end_hour, end_minute):
    #"{\"events\":[{\"mId\":\"id1\",\"mStartTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":15,\"hourOfDay\":15,\"minute\":15},
    # \"mEndTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":15,\"hourOfDay\":15,\"minute\":45},\"mName\":\"ev1\",\"mColor\":0,\"mAllDay\":false}"
    return {
        "mId": id,
        "mStartTime": time_dict_from_date_hour_and_minute(date, start_hour, start_minute),
        END_TIME_KEY: time_dict_from_date_hour_and_minute(date, end_hour, end_minute) if end_hour is not None else None,
        "mName": text,
        "mColor": 0,
        "mAllDay": False
    }


def time_dict_from_date_hour_and_minute(date, hour, minute):
    return {"year": date.year, "month": date.month-1, "dayOfMonth": date.day, "hourOfDay": hour, "minute": minute}


if __name__ == '__main__':
    args = read_args()

    events = []

    filename = args.wordfile.name
    basename, ext = os.path.splitext(filename)
    temp_files = []
    if ext == ".doc":
        os.system(f"soffice --convert-to docx {filename}")
        filename = os.path.split(filename)[1] + "x"
        temp_files.append(filename)
        # TODO convert to docx
    elif ext != ".docx":
        print("WRONG fileformat must be docx") # TODO change to doc
        exit(-1)
    document = Document(filename)
    table = document.tables[0]
    start_date = datetime.date(2018, 7, 28)
    time_pattern = re.compile(r"h\.[ \t]*(\d{1,2}):(\d{2})(-(\d{1,2}):(\d{2}))?\W*\n")
    for i, column in enumerate(table.columns):
        date = start_date + datetime.timedelta(i)
        cell_content = ""
        for cell in column.cells:
            if cell.text == cell_content:
                continue
            cell_content = cell.text
            match = re.search(time_pattern, cell.text.strip())
            if match is not None:
                start_hour = int(match.group(1))
                start_minute = int(match.group(2))
                end_hour = match.group(4)
                end_minute = match.group(5)
                try:
                    end_hour = int(end_hour)
                    end_minute = int(end_minute)
                except TypeError:
                    pass
                event_content = cell.text[match.span()[1]:]  # everything after time
                print(event_content + "\n")
                event = create_event(f"ev_{len(events)}", event_content, date, start_hour, start_minute, end_hour,
                                     end_minute)
                events.append(event)
                if len(events) > 1 and events[-2][END_TIME_KEY] is None:  # No endtime for previous event given
                    # use start time of next event as end time
                    events[-2][END_TIME_KEY] = time_dict_from_date_hour_and_minute(date, start_hour, start_minute)
        if len(events) > 0 and events[-1][END_TIME_KEY] is None:
            events[-1][END_TIME_KEY] = time_dict_from_date_hour_and_minute(date, LATEST_END_HOUR, 0)
    horarium_dict = {"events": events}
    horarium_dump = json.dumps(horarium_dict)
    with open(basename + ".json", "w+") as json_file:
        json_file.write(horarium_dump)
    for temp_file in temp_files:
        os.remove(temp_file)
