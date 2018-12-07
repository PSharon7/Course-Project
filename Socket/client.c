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


#define PORT "25518"	//AWS TCP PORT NUMBER FOR CLIENT

#define LOCALHOST "127.0.0.1"

// from www.beej.us/guide/bgnet/html/multi/clientserver.html
// get sockaddr, IPv4 or IPv6:
void *get_in_addr(struct sockaddr *sa) {
	if (sa->sa_family == AF_INET) {
    	return &(((struct sockaddr_in*)sa)->sin_addr);
    }

	return &(((struct sockaddr_in6*)sa)->sin6_addr);
}

int main(int argc, char const *argv[]) {
	/* code */
	int link_id, size, power;
	if(argc != 4) {
		fprintf(stderr, "ERROR : You should use ./client <LINK_ID> <SIZE> <POWER>.\n");
		exit(1);
	}
	else {
		// need error checking
		link_id = atoi(argv[1]);
		size = atoi(argv[2]);
		power = atoi(argv[3]);
	}

	// set up TCP
	// from www.beej.us/guide/bgnet/html/multi/clientserver.html
	int sockfd;
	struct addrinfo hints, *servinfo, *p;
	int rv;

	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;


	if ((rv = getaddrinfo(LOCALHOST, PORT, &hints, &servinfo)) != 0) {
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
		return 1;
	}

	for(p = servinfo; p != NULL; p = p->ai_next) {
		if ((sockfd = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) == -1) {
			perror("Client : socket");
			continue;
		}

		if (connect(sockfd, p->ai_addr, p->ai_addrlen) == -1) {
			close(sockfd);
			perror("Client : connect");
			continue;
		}

		break;
	}

	if (p == NULL) {
		fprintf(stderr, "client: failed to connect\n");
		return 2;
	}

	freeaddrinfo(servinfo); // all done with this structure
	printf("The client is up and running.\n");

	
	if (send(sockfd, &link_id, sizeof(link_id), 0) == -1) {
		fprintf(stderr, "error: send LINK_ID");
		exit(1);
	}
	if (send(sockfd, &size, sizeof(size), 0) == -1) {
		fprintf(stderr, "error: send SIZE");
		exit(1);
	}
	if (send(sockfd, &power, sizeof(power), 0) == -1) {
		fprintf(stderr, "error: send POWER");
		exit(1);
	}
	printf("The client sent ID=<%d>, size=<%d>, and power=<%d> to AWS\n", link_id, size, power );
	
	int match;
	double delay;
	if (recv(sockfd, &match, sizeof(match), 0) == -1) {
		fprintf(stderr, "error: recv MATCH");
		exit(1);
	}

	if (match == 1) {
		if (recv(sockfd, &delay, sizeof(delay), 0) == -1) {
			fprintf(stderr, "error: recv MATCH");
			exit(1);
		}
		printf("The delay for link <%d> is <%.2f>ms\n", link_id, delay);
	}
	else {
		printf("Found no matches for link <%d>\n", link_id);
	}
	
	close(sockfd);
	
	return 0;
}