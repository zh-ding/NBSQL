import socket
import threading
import struct
import time



class MyThread(threading.Thread):
    def __init__(self, arg):
        super(MyThread, self).__init__()
        self.arg = arg

    def run(self):
        sql = "update tb set name='%s' where name='test';"%self.arg
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.connect(('127.0.0.1', 59898))
        size = len(sql)
        client.send(struct.pack("!H", size))
        client.send(sql.encode())
        print(client.recv(400).decode())
        time.sleep(2)

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(('127.0.0.1', 59898))

sql = "insert into tb values(6);"
size = len(sql)

client.send(struct.pack("!H", size))
client.send(sql.encode())
print(client.recv(400).decode())

# for i in range(1000):
#     sql = "insert into tb values(%d, 'test');"%i
#     size = len(sql)
#     client.send(struct.pack("!H", size))
#     client.send(sql.encode())
#     print(client.recv(400).decode())

# sql = "update tb where name='test' set name='test_test';"
# size = len(sql)
# client.send(struct.pack("!H", size))
# client.send(sql.encode())
# while True:
#     print(client.recv(400).decode())
# time.sleep(1)
# t1 = MyThread("t1")
# t2 = MyThread("t2")
# t1.start()
# time.sleep(0.1)
# t2.start()

while True:
    pass
#client.close()