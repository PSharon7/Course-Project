import time

start = time.clock()
f_in = open("input.txt", "r");

groupCount = 0
potCount = 0
potDivision = []
teamPot = dict()
teamContinent = dict()

teamsConfederation = dict()
confederationIndex = dict()

potCons = []
continentCons = []


def check(team, potCon, continentCon):
    if potCon[teamPot[team]] > 0 and continentCon[confederationIndex[teamContinent[team]]] > 0:
        return True
    else:
        return False


def backtrack(assignment, teams):
    end = time.clock()
    if int(end-start) >= 160:
        return []

    if len(teams) == 0:
        return assignment

    team = teams[0]
    # print groupCount
    for i in range(groupCount):
        if check(team, potCons[i], continentCons[i]):
            assignment[i].append(team)
            potCons[i][teamPot[team]]-=1
            continentCons[i][confederationIndex[teamContinent[team]]]-=1

            # print team
            # print teams
            # print assignment
            # print 

            result = backtrack(assignment, teams[1:])
            if result != []:
                return result

            potCons[i][teamPot[team]]+=1
            continentCons[i][confederationIndex[teamContinent[team]]]+=1
            assignment[i].pop()
    return []

    
# Main function
if __name__ == '__main__':

    # read the data from input.txt
    groupCount = int(f_in.readline().rstrip())
    potCount = int(f_in.readline().rstrip())
    potDivision = [[] for i in range(potCount)]
    # teamsConfederation = dict()
    teams = []
    potSize = 0
    uefaSize = 0
    otherSize = 0
  
    for i in range(potCount):
        potDivision[i] = [j for j in f_in.readline().rstrip().split(",")]
        potSize = max(potSize, len(potDivision[i]))
        for j in potDivision[i]:
            teams.append(j)
            teamPot[j] = i

    # print teams
    assignment = []
    result = []

    for i in range(6):
        tmp = [j for j in f_in.readline().rstrip().split(":")]
        teamsConfederation[tmp[0]] = [j for j in tmp[1].split(",")]
        if tmp[0] == "UEFA":
            uefaSize = len(teamsConfederation[tmp[0]])
        else:
            otherSize = max(otherSize, len(teamsConfederation[tmp[0]]))

    # print "uefaSize",uefaSize
    # print "otherSize",otherSize
    # print "groupCount",groupCount
    # print "potSize",potSize


    if potSize <= groupCount and uefaSize <= groupCount*2 and otherSize <= groupCount:
        i = 0
        for key in teamsConfederation:
            if teamsConfederation[key] != ['None']:
                for t in teamsConfederation[key]:
                    teamContinent[t] = key
            confederationIndex[key] = i
            i += 1


        potCons = [[1 for col in range(potCount)] for row in range(groupCount)]
        continentCons = [[1 for col in range(6)] for row in range(groupCount)]
        for i in range(groupCount):
            continentCons[i][confederationIndex["UEFA"]] = 2

        assignment = [[] for i in range(groupCount)]
        result = backtrack(assignment, teams)
    

    # print groupCount

    # print potSize
    # print potCount
    # print potDivision
    # print teams
    # print "teamsConfederation",teamsConfederation
    # print "teamPot", teamPot
    # print "confederationIndex",confederationIndex
    # print "teamContinent",teamContinent
    # print "potCons", potCons
    # print "continentCons", continentCons


    if result != []:
        for i in range(len(result)):
            if result[i] == []:
                result[i] = ["None"]

    # print "assignment", assignment
    print "result", result


    try:
        with open("output.txt", "w") as f_out:
            if result == []:
                f_out.write("No")
            else:
                f_out.write("Yes" + "\n")
                for i in result:
                    for j in i[0:-1]:
                        f_out.write(str(j) + ",")
                    for j in i[-1]:
                        f_out.write(str(j))
                    f_out.write("\n")

        f_in.close()
    except:
        pass
    