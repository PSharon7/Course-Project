# the compiler: gcc for C program, define as g++ for C++
CC = gcc
# compiler flags:
CFLAGS = -g -Wall

# the build target executable:
TARGET = client_compile monitor_compile aws_compile serverA_compile serverB_compile serverC_compile

all: $(TARGET)

serverA_compile: serverA.c
	$(CC) $(CFLAGS) -o serverAoutput serverA.c

serverB_compile: serverB.c
	$(CC) $(CFLAGS) -o serverBoutput serverB.c

serverC_compile: serverC.c
	$(CC) $(CFLAGS) -o serverCoutput serverC.c -lm

aws_compile: aws.c
	$(CC) $(CFLAGS) -o awsoutput aws.c

client_compile: client.c
	$(CC) $(CFLAGS) -o client client.c

monitor_compile: monitor.c
	$(CC) $(CFLAGS) -o monitoroutput monitor.c

serverA:
	./serverAoutput

serverB:
	./serverBoutput

serverC:
	./serverCoutput

aws:
	./awsoutput

monitor:
	./monitoroutput

clean:
	$(RM) client monitoroutput awsoutput serverAoutput serverBoutput serverCoutput
