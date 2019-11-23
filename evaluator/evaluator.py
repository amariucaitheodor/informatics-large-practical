#!/usr/bin/env python3

import os
import sys
from subprocess import DEVNULL, check_call, CalledProcessError
import time
import json
import multiprocessing
import http.server
import socketserver

import xlsxwriter
from xlsxwriter.utility import xl_rowcol_to_cell

HTTP_PORT = 1920

years = [2019, 2020]
csvrows = []

def date_generator():
	days_of_month = {
		 1: list(range(1, 31 + 1)),
		 2: list(range(1, 28 + 1)),
		 3: list(range(1, 31 + 1)),
		 4: list(range(1, 30 + 1)),
		 5: list(range(1, 31 + 1)),
		 6: list(range(1, 30 + 1)),
		 7: list(range(1, 31 + 1)),
		 8: list(range(1, 31 + 1)),
		 9: list(range(1, 30 + 1)),
		10: list(range(1, 31 + 1)),
		11: list(range(1, 30 + 1)),
		# December is 31 days long but since 2019-12-31 is missing
		# consider it 30 days...
		12: list(range(1, 30 + 1))
	}

	for year in years:
		for month in range(1, 12 + 1):
			for day in days_of_month[month]:
				yield (day, month, year)


def run():
	for day, month, year in date_generator():
		start = time.perf_counter()
		check_call([
			"java", 
			"-jar", 
			"../powergrab/target/powergrab-0.0.1-SNAPSHOT.jar", 
			"{day:02}".format(day=day),
			"{month:02}".format(month=month),
			"%d" % year,
			"55.944425",
			"-3.188396",
			"5678",
			"stateful",
		], stdout=DEVNULL, stderr=DEVNULL, env=dict(os.environ, PG_HOST="127.0.0.1:%d" % (HTTP_PORT)))
		elapsed = time.perf_counter() - start
		print("\r%02d-%02d-%d  (%3.2f seconds)" % (day, month, year, elapsed), end="")

		geojson_path = "stateful-{day:02}-{month:02}-{year}.geojson".format(day=day, month=month, year=year)
		txt_path = "stateful-{day:02}-{month:02}-{year}.txt".format(day=day, month=month, year=year)
		
		with open(txt_path) as fd:
			last_line = fd.readlines()[-1]
			tokens = last_line.strip().split(",")
			coins = float(tokens[-2])
		
		coinsum = sum_coins(year, month, day)
		
		os.unlink(geojson_path)
		os.unlink(txt_path)

		csvrows.append((year, month, day, elapsed, coins, coinsum))

		if coinsum - coins > coinsum * 0.1:
			print("\n\t!!! %4d out of %4d  (%02.2f)" % (int(coins), int(coinsum), coins / coinsum * 100))

	print()


def sum_coins(year: int, month: int, day: int) -> float:
	with open("maps/stg/powergrab/{year}/{month:02}/{day:02}/powergrabmap.geojson".format(year=year, month=month, day=day)) as jfd:
		powergrabmap = json.load(jfd)

	l1 = map(float, (f["properties"]["coins"] for f in powergrabmap["features"]))
	return sum(e for e in l1 if e > 0)


def save():
	workbook = xlsxwriter.Workbook('report.xlsx')
	worksheet = workbook.add_worksheet()

	bold_format = workbook.add_format({'bold': True})
	perc_format = workbook.add_format()
	perc_format.set_num_format("0.00%")
	flpn_format = workbook.add_format()
	flpn_format.set_num_format("0.00")

	header = ("Year", "Month", "Day", "Time Elapsed (s)", "Coins Collected", "Max Coins", "Ratio")
	for col_i, cell in enumerate(header):
		worksheet.write(0, col_i, cell, bold_format)

	for row_i, row in enumerate(csvrows, start=1):
		for col_i, cell in enumerate(row):
			if 3 <= col_i <= 5:
				worksheet.write(row_i, col_i, cell, flpn_format)
			else:
				worksheet.write(row_i, col_i, cell)
		worksheet.write_formula(row_i, len(row), '=%s/%s' % (xl_rowcol_to_cell(row_i, 4), xl_rowcol_to_cell(row_i, 5)), perc_format)

	worksheet.write("I2", "Minimum Ratio", bold_format)
	worksheet.write_formula("J2", "=MIN(G2:G%d)" % (len(csvrows) + 1,), perc_format)

	worksheet.write("I3", "Median Ratio", bold_format)
	worksheet.write_formula("J3", "=MEDIAN(G2:G%d)" % (len(csvrows) + 1,), perc_format)

	worksheet.write("I4", "Average Ratio", bold_format)
	worksheet.write_formula("J4", "=AVERAGE(G2:G%d)" % (len(csvrows) + 1,), perc_format)

	worksheet.write("I6", "Maximum Elapsed", bold_format)
	worksheet.write_formula("J6", "=MAX(D2:D%d)" % (len(csvrows) + 1,), flpn_format)

	worksheet.write("I7", "Median Elapsed", bold_format)
	worksheet.write_formula("J7", "=MEDIAN(D2:D%d)" % (len(csvrows) + 1,), flpn_format)

	worksheet.write("I8", "Average Elapsed", bold_format)
	worksheet.write_formula("J8", "=AVERAGE(D2:D%d)" % (len(csvrows) + 1,), flpn_format)

	worksheet.autofilter(0, 0, len(csvrows), len(header) - 1)

	worksheet.conditional_format('G2:G%d' % (len(csvrows) + 1), {
		'type': '2_color_scale',
		"min_value": 0.9,
		"max_value": 1,
		"min_color": "#ff0000",
		"max_color": "#00ff00",
	})

	workbook.close()


def startHTTPServer():
	def serveHTTP():
		os.system("cd maps/ && python3 -m http.server %d > /dev/null 2>&1" % (HTTP_PORT,))

		"""
		handler = http.server.SimpleHTTPRequestHandler
		with socketserver.TCPServer(("127.0.0.1", HTTP_PORT), handler) as httpd:
			httpd.serve_forever()
		"""

	p = multiprocessing.Process(target=serveHTTP)
	p.start()
	return p


if __name__ == "__main__":
	try:
		p = startHTTPServer()
		run()
		p.terminate()
		save()
		print("\ndone.")
	except KeyboardInterrupt:
		sys.exit(1)
	except CalledProcessError as e:
		print(" ".join(e.cmd))
		raise e
	except Exception as e:
		raise e
