import socket
import threading
import struct
import time

ip = '127.0.0.1'
port = 12307

class MyThread(threading.Thread):
    def __init__(self, arg):
        super(MyThread, self).__init__()
        self.arg = arg

    def run(self):
        sql = "update tb set name='%s' where name='test';"%self.arg
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.connect((ip, port))
        size = len(sql)
        client.send(struct.pack("!H", size))
        client.send(sql.encode())
        print(client.recv(400).decode())
        print(client.recv(400).decode())
        time.sleep(2)

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect((ip, port))

sql = "use database test;"
size = len(sql)
client.send(struct.pack("!H", size))
client.send(sql.encode())
print(client.recv(400).decode())
print(client.recv(400).decode())

sql = "create table tb (id int, name string(32));"
size = len(sql)
client.send(struct.pack("!H", size))
client.send(sql.encode())
print(client.recv(400).decode())
print(client.recv(400).decode())

for i in range(100):
    sql = "insert into tb values(%d, 'test');"%i
    size = len(sql)
    client.send(struct.pack("!H", size))
    client.send(sql.encode())
    print(client.recv(400).decode())
    print(client.recv(400).decode())

t1 = MyThread("t1")
t2 = MyThread("t2")
t1.start()
t2.start()

t1.join()
t2.join()
