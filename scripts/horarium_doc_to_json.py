import argparse
import datetime
import json
import numpy as np
import os
import re
from docx.api import Document
from tabula.io import read_pdf

LATEST_END_HOUR = 23
END_TIME_KEY = "mEndTime"


def read_args():
    opts = argparse.ArgumentParser(description='Create a json file which can be loaded in Septimanapp '
                                               'from a word file containing the Horarium')
    opts.add_argument('wordfile', metavar='WORD_FILE', type=argparse.FileType("r"),
                      help='path to word file with the Horarium')
    opts.add_argument('date', metavar='FIRST_DAY', type=str,
                      help='First day of the septimana (DD.MM.YY)')
    opts.add_argument('language', metavar='Language', choices=["de", "la"],
                      help='The language the Horarium is in')
    opts.add_argument("--table_index", type=int, default=0)
    args = opts.parse_args()
    return args


def create_event(id, text, date, start_hour, start_minute, end_hour, end_minute):
    return {
        "mId": id,
        "mStartTime": time_dict_from_date_hour_and_minute(date, start_hour, start_minute),
        END_TIME_KEY: time_dict_from_date_hour_and_minute(date, end_hour, end_minute) if end_hour is not None else None,
        "mName": text,
        "mColor": 0,
        "mAllDay": False
    }


def time_dict_from_date_hour_and_minute(date, hour, minute):
    return {"year": date.year, "month": date.month - 1, "dayOfMonth": date.day, "hourOfDay": hour, "minute": minute}


def combine_items_for_same_time(column):
    time_regex = re.compile(r"(h[:.][ \t]*(\d{1,2}[:.]\d{2} ?[-—–‒―]? ?){1,2})"
                            r"|([ \t]*(\d{1,2}[:.]\d{2} ?(Uhr)? ?[-—–‒―]? ?){1,2})[ \t]?Uhr")
    indices_with_time = [i for i, txt in enumerate(column) if re.search(time_regex, txt)] + [len(column)]
    column = ["\n".join(column[index:next_index]) for index, next_index in
              zip(indices_with_time[:-1], indices_with_time[1:])]
    return column


if __name__ == '__main__':

    args = read_args()

    events = []
    filename = args.wordfile.name
    basename, ext = os.path.splitext(filename)
    temp_files = []
    if ext == ".pdf":
        # noinspection PyTypeChecker
        df = read_pdf(filename, stream=True)[args.table_index].replace("\r", "\n").replace(np.nan, "")
        text_table = [combine_items_for_same_time([text for text in column if len(text) > 0])
                      for column in df.transpose().values]
    else:
        if ext == ".doc":
            os.system(f"soffice --convert-to docx {filename}")
            filename = os.path.split(filename)[1] + "x"
            temp_files.append(filename)
        elif ext != ".docx":
            print("WRONG fileformat must be docx, doc or pdf")
            exit(-1)
        document = Document(filename)
        table = document.tables[args.table_index]
        text_table = [combine_items_for_same_time([cell.text for cell in column.cells]) for column in table.columns]

    try:
        date = [int(d) for d in args.date.split(".")]
        if date[2] < 1000:
            date[2] += 2000
    except ValueError:
        print("WRONG date format, should be (DD.MM.YY)")
        exit(-1)
    # noinspection PyUnboundLocalVariable
    start_date = datetime.date(*reversed(date))

    locale = args.language
    time_pattern = re.compile(r"h[.:]?[ \t]*(\d{1,2})[:.](\d{2})[ ]*([-—–‒―][ ]*(\d{1,2})[:.](\d{2}))?\W*\n"
                              r"|[ \t]*(\d{1,2})[:.](\d{2})( ?[-—–‒―] ?(\d{1,2})[:.](\d{2}))?( [Uu]hr)?\W*\n?")
    for i, column in enumerate(text_table):
        date = start_date + datetime.timedelta(i)
        cell_content = ""
        for cell in column:
            if cell == cell_content:
                continue
            cell_content = cell
            match = re.search(time_pattern, cell.strip())
            if match is not None:
                group_index_shift = 0
                if match.group(1) is None:
                    group_index_shift = 5
                start_hour = int(match.group(1 + group_index_shift))
                start_minute = int(match.group(2 + group_index_shift))
                end_hour = match.group(4 + group_index_shift)
                end_minute = match.group(5 + group_index_shift)
                try:
                    end_hour = int(end_hour)
                    end_minute = int(end_minute)
                except TypeError:
                    pass
                event_content = cell[match.span()[1]:]  # everything after time
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
    json_filename = f"horarium_{date.year}_{locale}.json"
    folder, _ = os.path.split(basename)
    with open(os.path.join(folder, json_filename), "w+") as json_file:
        json_file.write(horarium_dump)
    for temp_file in temp_files:
        os.remove(temp_file)
