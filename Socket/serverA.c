/*
 * Storage Server A
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

#define MYPORT "21518"
#define LOCALHOST "127.0.0.1"


double database_a[5];

// from www.beej.us/guide/bgnet/html/multi/clientserver.html
// get sockaddr, IPv4 or IPv6:
void *get_in_addr(struct sockaddr *sa) {
	if (sa->sa_family == AF_INET) {
    	return &(((struct sockaddr_in*)sa)->sin_addr);
    }

	return &(((struct sockaddr_in6*)sa)->sin6_addr);
}


int search(int link_id) {
	FILE *fp = fopen("database_a.csv", "r");
	if (fp == NULL) {
		exit(0);
	}

	char line[255], *token;
	int i = 0;
	while(!feof(fp)) {
		fscanf(fp, "%s", line);
		token = strtok(line, ",");

		if(strtod(token, NULL) == (double)link_id){
			database_a[i++] = (double)link_id;
			token = strtok(NULL, ",");
			database_a[i++] = strtod(token, NULL);
			token = strtok(NULL, ",");
			database_a[i++] = strtod(token, NULL);
			token = strtok(NULL, ",");
			database_a[i++] = strtod(token, NULL);
			token = strtok(NULL, ",");
			database_a[i++] = strtod(token, NULL);
			return 1;
		}
	}
	
	fclose(fp);
	return 0;
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
			perror("serverA: socket");
			continue;
		}

		if (bind(sockfd, p->ai_addr, p->ai_addrlen) == -1) {
			close(sockfd);
			perror("serverA: bind");
			continue;
		}

		break;
	}

	if (p == NULL) {
		fprintf(stderr, "serverA: failed to bind socket\n");
		return 2;
	}

	freeaddrinfo(servinfo); // all done with this structure
	printf("The Server A is up and running using UDP on port <%s>.\n", MYPORT);

	addr_len = sizeof their_addr;

	while(1) {  // main accept() loop

		int link_id;

		if (recvfrom(sockfd, &link_id, sizeof link_id, 0, (struct sockaddr *)&their_addr, &addr_len) == -1) {
			perror("serverA: recvfrom");
			exit(1);
		}

		printf("The Server A received input <%d>\n", link_id);

		int match = search(link_id);

		printf("The Server A has found <%d> match\n", match);

		if (sendto(sockfd, &match, sizeof match, 0, (struct sockaddr *)&their_addr, addr_len) == -1) {
			perror("serverA: sendto");
			exit(1);
		}

		if (match == 1 && sendto(sockfd, &database_a, sizeof database_a, 0, (struct sockaddr *)&their_addr, addr_len) == -1) {
			perror("serverA: sendto");
			exit(1);
		}

		printf("The Server A finished sending the output to AWS\n");

	}
	
	close(sockfd);

	return 0;
}