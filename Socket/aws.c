#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <netdb.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/wait.h>

#define PORTA "21518"			//SERVER A UDP PORT NUMBER
#define PORTB "22518"			//SERVER B UDP PORT NUMBER
#define PORTC "23518"			//SERVER C UDP PORT NUMBER

#define UDPPORT "24518"			//AWS UDP PORT NUMBER
#define TCPPORT_CLI "25518"		//AWS TCP PORT NUMBER FOR CLIENT
#define TCPPORT_MON "26518"		//AWS TCP PORT NUMBER FOR MONITOR

#define LOCALHOST "127.0.0.1"
#define BACKLOG 10

double data[5];
double transTime, propTime, delay;

// from www.beej.us/guide/bgnet/html/multi/clientserver.html
// get sockaddr, IPv4 or IPv6:
void *get_in_addr(struct sockaddr *sa) {
	if (sa->sa_family == AF_INET) {
    	return &(((struct sockaddr_in*)sa)->sin_addr);
    }

	return &(((struct sockaddr_in6*)sa)->sin6_addr);
}

int search(char server, int link_id) {
	char server_name[2];
	char *server_port;
	if (server == 'A') {
		server_port = PORTA;
		strcpy(server_name, "A");
	}
	else if (server == 'B') {
		server_port = PORTB;
		strcpy(server_name, "B");
	}

	int mysock;
	struct addrinfo hints, *servinfo, *p;
	int rv;

	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_DGRAM;

	if ((rv = getaddrinfo(LOCALHOST, server_port, &hints, &servinfo)) != 0) {
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
		return 1;
	}

	for(p = servinfo; p != NULL; p = p->ai_next) {
		if ((mysock = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) == -1) {
			perror("AWS talker: socket");
			continue;
		}

		break;
	}

	if (p == NULL) {
		fprintf(stderr, "AWS talker: failed to bind\n");
		return 2;
	}

	if (sendto(mysock, &link_id, sizeof link_id, 0, p->ai_addr, p->ai_addrlen) == -1) {
		perror("AWS: sendto");
		exit(1);
	}

	printf("The AWS sent link ID=<%d> to Backend-Server <%s> using UDP over port <%s>\n", link_id, server_name, UDPPORT);

	int match;
	if (recvfrom(mysock, &match, sizeof match, 0, NULL, NULL) == -1) {
		perror("AWS: recvfrom");
		exit(1);
	}

	if (match == 1) {
		if (recvfrom(mysock, &data, sizeof data, 0, NULL, NULL) == -1) {
			perror("AWS: recvfrom");
			exit(1);
		}
	}

	printf("The AWS received <%d> matches from Backend-Server <%s> using UDP over port <%s>\n", match, server_name, UDPPORT);

	return match;
}

double calculation(int link_id, int size, int power) {
	int mysock;
	struct addrinfo hints, *servinfo, *p;
	int rv;

	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_DGRAM;

	if ((rv = getaddrinfo(LOCALHOST, PORTC, &hints, &servinfo)) != 0) {
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
		return 1;
	}

	for(p = servinfo; p != NULL; p = p->ai_next) {
		if ((mysock = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) == -1) {
			perror("AWS talker: socket");
			continue;
		}

		break;
	}

	if (p == NULL) {
		fprintf(stderr, "AWS talker: failed to bind\n");
		return 2;
	}

	
	if (sendto(mysock, &link_id, sizeof link_id, 0, p->ai_addr, p->ai_addrlen) == -1) {
		perror("AWS: sendto");
		exit(1);
	}

	if (sendto(mysock, &size, sizeof size, 0, p->ai_addr, p->ai_addrlen) == -1) {
		perror("AWS: sendto");
		exit(1);
	}

	if (sendto(mysock, &power, sizeof power, 0, p->ai_addr, p->ai_addrlen) == -1) {
		perror("AWS: sendto");
		exit(1);
	}

	if (sendto(mysock, &data, sizeof data, 0, p->ai_addr, p->ai_addrlen) == -1) {
		perror("AWS: sendto");
		exit(1);
	}

	printf("The AWS sent link ID=<%d>, size=<%d>, power=<%d>, and link information to Backend-Server C using UDP over port <%s>\n", link_id, size, power, UDPPORT);

	if (recvfrom(mysock, &transTime, sizeof transTime, 0, NULL, NULL) == -1) {
		perror("AWS: recvfrom");
		exit(1);
	}
	if (recvfrom(mysock, &propTime, sizeof propTime, 0, NULL, NULL) == -1) {
		perror("AWS: recvfrom");
		exit(1);
	}
	if (recvfrom(mysock, &delay, sizeof delay, 0, NULL, NULL) == -1) {
		perror("AWS: recvfrom");
		exit(1);
	}

	printf("The AWS received outputs from Backend-Server C using UDP over port <%s>\n", UDPPORT);

	return delay;
}


int main(int argc, char const *argv[]) {
	/* code */
	// from www.beej.us/guide/bgnet/html/multi/clientserver.html
	
	int sockfd, new_fd;
	struct addrinfo hints, *servinfo, *p;
	struct sockaddr_storage their_addr; // connector's address information
	socklen_t sin_size;
	int yes = 1;
	int rv;


	int sockfd2;
	struct addrinfo hints2, *servinfo2, *p2;
	struct sockaddr_storage their_addr2;
	socklen_t sin_size2;
	int yes2 = 1;
	int rv2;

	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE; // use my IP


	memset(&hints2, 0, sizeof hints2);
	hints2.ai_family = AF_UNSPEC;
	hints2.ai_socktype = SOCK_STREAM;
	hints2.ai_flags = AI_PASSIVE;


	if ((rv = getaddrinfo(LOCALHOST, TCPPORT_CLI, &hints, &servinfo)) != 0) {
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
		return 1;
	}

	if ((rv2 = getaddrinfo(LOCALHOST, TCPPORT_MON, &hints2, &servinfo2)) != 0) {
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv2));
		return 1;
	}

	// loop through all the results and bind to the first we can
	for(p = servinfo; p != NULL; p = p->ai_next) {
		if ((sockfd = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) == -1) {
			perror("AWS: socket");
			continue;
		}

		if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1) {
			perror("setsockopt");
			exit(1);
		}

		if (bind(sockfd, p->ai_addr, p->ai_addrlen) == -1) {
			close(sockfd);
			perror("server: bind");
			continue;
		}

		break;
	}

	// loop through all the results and bind to the first we can
	for(p2 = servinfo2; p2 != NULL; p2 = p2->ai_next) {
		if ((sockfd2 = socket(p2->ai_family, p2->ai_socktype, p2->ai_protocol)) == -1) {
			perror("AWS: socket");
			continue;
		}

		if (setsockopt(sockfd2, SOL_SOCKET, SO_REUSEADDR, &yes2, sizeof(int)) == -1) {
			perror("setsockopt");
			exit(1);
		}

		if (bind(sockfd2, p2->ai_addr, p2->ai_addrlen) == -1) {
			close(sockfd2);
			perror("server: bind");
			continue;
		}

		break;
	}

	if (p == NULL) {
		fprintf(stderr, "AWS: failed to bind\n");
		exit(1);
	}

	if (p2 == NULL) {
		fprintf(stderr, "AWS: failed to bind\n");
		exit(1);
	}

	freeaddrinfo(servinfo); // all done with this structure	
	freeaddrinfo(servinfo2); // all done with this structure

	//listen
	if (listen(sockfd, BACKLOG) == -1) {
		perror("listen");
		exit(1);
	}

	if (listen(sockfd2, BACKLOG) == -1) {
		perror("listen");
		exit(1);
	}

	printf("The AWS is up and running.\n");

	sin_size2 = sizeof their_addr2;
	sockfd2 = accept(sockfd2, (struct sockaddr *)&their_addr2, &sin_size2);
	if (sockfd2 == -1) {
		perror("accept");
		exit(1);
	}

	while(1) {  // main accept() loop
		sin_size = sizeof their_addr;

		new_fd = accept(sockfd, (struct sockaddr *)&their_addr, &sin_size);
		if (new_fd == -1) {
			perror("accept");
			exit(1);
		}

		if (!fork()) {
			close(sockfd);

			int link_id, size, power;
			if (recv(new_fd, &link_id, sizeof link_id, 0) == -1) {
				perror("AWS: recv");
				exit(1);
			}
			if (recv(new_fd, &size, sizeof size, 0) == -1) {
				perror("AWS: recv");
				exit(1);
			}
			if (recv(new_fd, &power, sizeof power, 0) == -1) {
				perror("AWS: recv");
				exit(1);
			}
			
			printf("The AWS received link ID=<%d>, size=<%d>, and power=<%d> from the client using TCP over port <%s>\n", link_id, size, power, TCPPORT_CLI);

			if (send(sockfd2, &link_id, sizeof(link_id), 0) == -1) {
				fprintf(stderr, "error: send LINK_ID");
				exit(1);
			}
			if (send(sockfd2, &size, sizeof(size), 0) == -1) {
				fprintf(stderr, "error: send SIZE");
				exit(1);
			}
			if (send(sockfd2, &power, sizeof(power), 0) == -1) {
				fprintf(stderr, "error: send POWER");
				exit(1);
			}
			printf("The AWS sent link ID=<%d>, size=<%d>, and power=<%d> to the monitor using TCP over port <%s>\n", link_id, size, power, TCPPORT_MON );

			int searchA = search('A', link_id);
			int searchB = search('B', link_id);

			int match = searchA | searchB;

			if (send(new_fd, &match, sizeof match, 0) == -1) {
				fprintf(stderr, "error: send to client");
				exit(1);
			}

			if (send(sockfd2, &match, sizeof match, 0) == -1) {
				fprintf(stderr, "error: send to monitor");
				exit(1);
			}


			if (match){
				double resultC = calculation(link_id, size, power);

				if (send(new_fd, &resultC, sizeof resultC, 0) == -1) {
					fprintf(stderr, "error: send");
					exit(1);
				}
				printf("The AWS sent delay=<%.2f>ms to the client using TCP over port <%s>\n", resultC, TCPPORT_CLI);
				
				if (send(sockfd2, &transTime, sizeof transTime, 0) == -1) {
					fprintf(stderr, "error: send");
					exit(1);
				}
				if (send(sockfd2, &propTime, sizeof propTime, 0) == -1) {
					fprintf(stderr, "error: send");
					exit(1);
				}
				if (send(sockfd2, &delay, sizeof delay, 0) == -1) {
					fprintf(stderr, "error: send");
					exit(1);
				}
				printf("The AWS sent detailed results to the monitor using TCP over port <%s>\n", TCPPORT_MON);

			}
			else {
				printf("The AWS sent No Match to the monitor and the client using TCP over ports <%s> and <%s>, respectively\n", TCPPORT_MON, TCPPORT_CLI);
			}

			close(new_fd);

			exit(0);


		}

		close(new_fd);  // parent doesn't need this
	}

	return 0;
}
