data1 = None
data2 = None
with open("Dwa") as file:
    data1 = [int(x) for x in file]
with open("Cztery") as file:
    data2 = [int(x) for x in file]

print(data1)
print(data2)

counter1 = -1
counter2 = 999

i = 0
j = 0

while i < 100 or j < 100:
    if data1[i] == counter1 + 1:
        counter1 += 1
        i += 1
        print(f'Counter 1: {counter1}')
    elif data1[i] == counter2 + 1:
        counter2 += 1
        i += 1
        print(f'Counter 2: {counter2}')
    elif data2[j] == counter1 + 1:
        counter1 += 1
        j += 1
        print(f'Counter 1: {counter1}')
    elif data2[j] == counter2 + 1:
        counter2 += 1
        j += 1
        print(f'Counter 2: {counter2}')
    else:
        print(f"Error at {counter1} {counter2}")
        break
