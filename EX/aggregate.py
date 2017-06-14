import csv

read_formatted = []
with open("data.csv", newline='') as csvfile:
    csvreader = csv.reader(csvfile)
    impl_map = {"1": "lazy", "2": "lock_free"}
    i = -1
    for row in csvreader:
        i += 1
        if i == 0:
            read_formatted.append(row)
            continue
        new_row = [
            int(row[0]),
            impl_map[row[1]],
            int(row[2]),
            float(row[3])
        ]
        read_formatted.append(new_row)

agg = [[read_formatted[0][0], read_formatted[0][1], "avg_through"]]
avg = 0
for i in range(1, len(read_formatted)):
    formatted_row = read_formatted[i]
    avg += formatted_row[3] / 10
    if i % 10 == 0:
        row = [formatted_row[0], formatted_row[1], int(avg)]
        agg.append(row)
        avg = 0

with open('aggs.csv', 'w', newline='') as csvfile:
    csvwriter = csv.writer(csvfile, quoting=csv.QUOTE_MINIMAL)
    for row in agg:
        csvwriter.writerow(row)
