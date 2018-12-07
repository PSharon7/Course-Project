/*
 * Computing Server C
 */

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
#include <math.h>

#define MYPORT "23518"
#define LOCALHOST "127.0.0.1"

double transTime = -1, propTime = -1, delay = -1;

// from www.beej.us/guide/bgnet/html/multi/clientserver.html
// get sockaddr, IPv4 or IPv6:
void *get_in_addr(struct sockaddr *sa) {
	if (sa->sa_family == AF_INET) {
    	return &(((struct sockaddr_in*)sa)->sin_addr);
    }

	return &(((struct sockaddr_in6*)sa)->sin6_addr);
}

// calculation
double transformDB2WATT(double DB) {
	return pow(10, DB/10 - 3);
}

void calculation(int link_id, int size, int power, double data[5]) {
	double S = transformDB2WATT((double)power);
	double N = transformDB2WATT(data[4]);
	double C = data[1] * 1000000 * log2f(1 + S/N);

	transTime = size / C * 1000;
	propTime = (data[2] / data[3]) / 10;
	delay = transTime + propTime;
}

int main(int argc, char const *argv[]) {
	/* code */
	// from www.beej.us/guide/bgnet/html/multi/clientserver.html
	int sockfd;	// listen on sock_fd
	struct addrinfo hints, *servinfo, *p;
	struct sockaddr_storage their_addr; // connector's address information
	int rv;
	socklen_t addr_len;

	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_DGRAM;
	hints.ai_flags = AI_PASSIVE; // use my IP

	if ((rv = getaddrinfo(NULL, MYPORT, &hints, &servinfo)) != 0) {
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
		return 1;
	}

	// loop through all the results and bind to the first we can
	for(p = servinfo; p != NULL; p = p->ai_next)  {
		if ((sockfd = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) == -1) {
			perror("serverB: socket");
			continue;
		}

		if (bind(sockfd, p->ai_addr, p->ai_addrlen) == -1) {
			close(sockfd);
			perror("serverB: bind");
			continue;
		}

		break;
	}

	if (p == NULL) {
		fprintf(stderr, "serverB: failed to bind socket\n");
		return 2;
	}

	freeaddrinfo(servinfo); // all done with this structure
	printf("The Server C is up and running using UDP on port <%s>.\n", MYPORT);

	addr_len = sizeof their_addr;

	while(1) {  // main accept() loop

		int link_id, size, power;
		double data[5];

		if (recvfrom(sockfd, &link_id, sizeof link_id, 0, (struct sockaddr *)&their_addr, &addr_len) == -1) {
			perror("serverC: recvfrom link_id");
			exit(1);
		}

		if (recvfrom(sockfd, &size, sizeof size, 0, (struct sockaddr *)&their_addr, &addr_len) == -1) {
			perror("serverC: recvfrom size");
			exit(1);
		}

		if (recvfrom(sockfd, &power, sizeof power, 0, (struct sockaddr *)&their_addr, &addr_len) == -1) {
			perror("serverC: recvfrom power");
			exit(1);
		}

		if (recvfrom(sockfd, &data, sizeof data, 0, (struct sockaddr *)&their_addr, &addr_len) == -1) {
			perror("serverC: recvfrom data");
			exit(1);
		}

		printf("The Server C received link information of link <%d>, file size <%d>, and signal power <%d>\n", link_id, size, power);

		calculation(link_id, size, power, data);

		printf("The server C finished the calculation for link <%d>\n", link_id);

		if (sendto(sockfd, &transTime, sizeof transTime, 0, (struct sockaddr *)&their_addr, addr_len) == -1) {
			perror("serverC: sendto");
			exit(1);
		}

		if (sendto(sockfd, &propTime, sizeof propTime, 0, (struct sockaddr *)&their_addr, addr_len) == -1) {
			perror("serverC: sendto");
			exit(1);
		}

		if (sendto(sockfd, &delay, sizeof delay, 0, (struct sockaddr *)&their_addr, addr_len) == -1) {
			perror("serverC: sendto");
			exit(1);
		}

		printf("The Server C finished sending the output to AWS\n");

	}

	close(sockfd);

	return 0;
}