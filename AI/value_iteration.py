import numpy as np
import time

f_in = open("input.txt", "r");

grid_world = []
policy_world = []

prev_world = []
rewards = []

max_idx = []
rewards_world = []

wall_grid_num = 0
wall_grid_pos = []

terminal_state_num = 0
terminal_state_pos = []
terminal_state_rew = []

max_change = float('inf')

n_row = 0
n_column = 0

pro_walk = 0
pro_run = 0
pro_walk_other = 0
pro_run_other = 0

reward = []
discount = 0
epsilon = 0

def action_calcu():
    #walk
    choice_world[0][n_row-1] = grid_world[n_row-1]; choice_world[0][0:n_row-1] = grid_world[1:]
    choice_world[1][0] = grid_world[0]; choice_world[1][1:] = grid_world[0:n_row-1]
    choice_world[2][:,0] = grid_world[:,0]; choice_world[2][:,1:] = grid_world[:,0:n_column-1]; 
    choice_world[3][:,n_column-1] = grid_world[:,n_column-1]; choice_world[3][:,0:n_column-1] = grid_world[:,1:]; 

    #run
    choice_world[4][n_row-2:] = grid_world[n_row-2:]; choice_world[4][0:n_row-2] = grid_world[2:]
    choice_world[5][0:2] = grid_world[0:2]; choice_world[5][2:] = grid_world[0:n_row-2]
    choice_world[6][:,0:2] = grid_world[:,0:2]; choice_world[6][:,2:] = grid_world[:,0:n_column-2]; 
    choice_world[7][:,n_column-2:] = grid_world[:,n_column-2:]; choice_world[7][:,0:n_column-2] = grid_world[:,2:];

    for i in range(wall_grid_num):
        x = wall_grid_pos[i,0]; y = wall_grid_pos[i,1];
        if x < n_row-1:
            choice_world[1][x+1][y] = grid_world[x+1][y]
            choice_world[5][x+1][y] = grid_world[x+1][y]
        if x < n_row-2:
            choice_world[5][x+2][y] = grid_world[x+2][y]

        if x > 0:
            choice_world[0][x-1][y] = grid_world[x-1][y]
            choice_world[4][x-1][y] = grid_world[x-1][y]
        if x > 1:
            choice_world[4][x-2][y] = grid_world[x-2][y]

        if y > 0:
            choice_world[3][x][y-1] = grid_world[x][y-1]
            choice_world[7][x][y-1] = grid_world[x][y-1]
        if y > 1:
            choice_world[7][x][y-2] = grid_world[x][y-2]

        if y < n_column-1:
            choice_world[2][x][y+1] = grid_world[x][y+1]
            choice_world[6][x][y+1] = grid_world[x][y+1]
        if y < n_column-2:
            choice_world[6][x][y+2] = grid_world[x][y+2]

def update_world():
    global max_idx, max_change, grid_world

    moves_pvs = np.array([
        #walkdown
        pro_walk * choice_world[0] + pro_walk_other * choice_world[2] + pro_walk_other * choice_world[3],
        #walkup
        pro_walk * choice_world[1] + pro_walk_other * choice_world[2] + pro_walk_other * choice_world[3],
        #walkleft
        pro_walk * choice_world[2] + pro_walk_other * choice_world[0] + pro_walk_other * choice_world[1],
        #walkright
        pro_walk * choice_world[3] + pro_walk_other * choice_world[0] + pro_walk_other * choice_world[1],
        #rundown
        pro_run * choice_world[4] + pro_run_other * choice_world[6] + pro_run_other * choice_world[7],
        #runup
        pro_run * choice_world[5] + pro_run_other * choice_world[6] + pro_run_other * choice_world[7],
        #runleft
        pro_run * choice_world[6] + pro_run_other * choice_world[4] + pro_run_other * choice_world[5],
        #runright
        pro_run * choice_world[7] + pro_run_other * choice_world[4] + pro_run_other * choice_world[5]
    ])

    moves_pvs_max = moves_pvs * discount + rewards_world

    max_idx = np.argmax(moves_pvs_max, axis = 0)

    tmp_world = np.max(moves_pvs_max, axis = 0)

    for i in range(wall_grid_num):
        tmp_world[wall_grid_pos[i,0]][wall_grid_pos[i,1]] = 0
    for i in range(terminal_state_num):
        tmp_world[terminal_state_pos[i,0]][terminal_state_pos[i,1]] = terminal_state_rew[i]

    max_change = np.max(np.abs(tmp_world - grid_world))
    # print max_change

    grid_world = tmp_world


# Main function
if __name__ == '__main__':

    # read the data from input.txt
    tmp = [j for j in f_in.readline().rstrip().split(",")]
    tmp = map(eval, tmp)
    n_row = tmp[0]
    n_column = tmp[1]

    grid_world = np.zeros((n_row, n_column))
    choice_world = np.zeros((8, n_row, n_column))   # 0 - 7 'Walk Up', 'Walk Down', 'Walk Left', 'Walk Right', 'Run up', 'Run down', 'Run Left', 'Run Right'

    is_terminal = np.zeros((n_row, n_column))
    is_block = np.zeros((n_row, n_column))
    max_idx = np.zeros((n_row, n_column), int)

    policy_world = [['' for col in range(n_column)] for row in range(n_row)]

    wall_grid_num = int(f_in.readline().rstrip())
    wall_grid_pos = np.zeros((wall_grid_num, 2), int)
    for i in range(wall_grid_num):
        tmp = [j for j in f_in.readline().rstrip().split(",")]
        tmp = map(eval, tmp)
        wall_grid_pos[i] = tmp

    wall_grid_pos -= 1
        
    terminal_state_num = int(f_in.readline().rstrip())
    terminal_state_pos = np.zeros((terminal_state_num, 2), int)
    terminal_state_rew = np.zeros((terminal_state_num)) 
    for i in range(terminal_state_num):
        tmp = [j for j in f_in.readline().rstrip().split(",")]
        tmp = map(eval, tmp)
        terminal_state_pos[i] = tmp[0: 2]
        terminal_state_rew[i] = tmp[-1]
        grid_world[terminal_state_pos[i][0]-1][terminal_state_pos[i][1]-1] = terminal_state_rew[i]

    terminal_state_pos -= 1

    max_terminal_id = np.argmax(terminal_state_rew)
    index_row = terminal_state_pos[i][0]
    index_col = terminal_state_pos[i][1]

    tmp = [j for j in f_in.readline().rstrip().split(",")]
    tmp = map(eval, tmp)
    pro_walk = tmp[0]
    pro_run = tmp[1]

    pro_walk_other = 0.5 * (1 - pro_walk)
    pro_run_other = 0.5 * (1 - pro_run)

    reward = [j for j in f_in.readline().rstrip().split(",")]
    reward = map(eval, reward)
    discount = float(f_in.readline().rstrip())
    rewards = np.array([reward[0],reward[0],reward[0],reward[0],reward[1],reward[1],reward[1],reward[1]])

    rewards_world = np.zeros((8, n_row, n_column))
    rewards_world[0:4] = reward[0]
    rewards_world[4:] = reward[1]

    iter_cnt = 1

    start = time.clock()
    end = time.clock()

    
    while (max_change > epsilon * (1-discount)/discount) and (end - start <= 170):
        print 'iteration {}'.format(iter_cnt).center(72, '-')
        max_change = 0

        action_calcu()
        update_world()

        # print max_change

        end = time.clock()
        iter_cnt += 1

    moves_directions = ['Walk Up', 'Walk Down', 'Walk Left', 'Walk Right', 'Run Up', 'Run Down', 'Run Left', 'Run Right']

    for i in range(n_row):
        for j in range(n_column):
            policy_world[i][j] = moves_directions[max_idx[i][j]]

    for i in range(wall_grid_num):
        policy_world[wall_grid_pos[i,0]][wall_grid_pos[i,1]] = "None"
    for i in range(terminal_state_num):
        policy_world[terminal_state_pos[i,0]][terminal_state_pos[i,1]] = "Exit"

    # for x in reversed(policy_world):
    #     print x

    try:
        with open("output.txt", "w") as f_out:
            for i in reversed(policy_world):
                for j in i[0:-1]:
                    f_out.write(str(j) + ",")
                for j in i[-1]:
                    f_out.write(str(j))
                f_out.write("\n")

        f_in.close()
    except:
        pass
    