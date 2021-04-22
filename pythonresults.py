name = raw_input()
title_list = name.split("Instance: ")
title_list_2 = title_list[1].split(" Time:")
title = title_list_2[0]
# print("This is title: ", title)
result_list = title_list_2[1].split("Result: ")
result_list_2 = result_list[1].split(" Solution:")
result = result_list_2[0]
# print("Result: ",result)

data = result_list_2[1].split(" 0 0 ")
data.pop(0)
for i in range (0, len(data)):
  data[i] = '0 ' + data[i]
  if i != len(data) -1:
    data[i] = data[i] + ' 0'

result = result + ' 0'
# print(result)

final = result + '\n'
final = final + '\n'.join(data)

# print(final)

with open('output/' + title, 'w') as f:
  f.write(final)