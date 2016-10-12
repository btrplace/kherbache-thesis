/* Copyright (c) 2007-2015. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

#include <stdio.h>
#include <stdlib.h>
#include <semaphore.h>
#include "simgrid/msg.h"
#include "xbt/sysdep.h"         /* calloc, printf */

/* Create a log channel to have nice outputs. */
#include "xbt/log.h"
#include "xbt/asserts.h"
XBT_LOG_NEW_DEFAULT_CATEGORY(msg_test,
                             "Messages specific for this msg example");

#include "../json-parser/json-parser.h"

FILE *gl_output_file = NULL;
sem_t gl_output_file_sem;

static void vm_migrate(msg_vm_t vm, msg_host_t dst_pm)
{
  msg_host_t src_pm = MSG_vm_get_pm(vm);
  double mig_sta = MSG_get_clock();
  MSG_vm_migrate(vm, dst_pm);
  double mig_end = MSG_get_clock();

  XBT_INFO("===>>>> %s migrated: %s->%s in %g s time now is %g", MSG_vm_get_name(vm),
	   MSG_host_get_name(src_pm), MSG_host_get_name(dst_pm),
	   mig_end - mig_sta, mig_end);
}

static int migration_worker_main(int argc, char *argv[])
{
  xbt_assert(argc == 3);
  char *vm_name = argv[1];
  char *dst_pm_name = argv[2];

  msg_vm_t vm = MSG_host_by_name(vm_name);
  msg_host_t dst_pm = MSG_host_by_name(dst_pm_name);

  vm_migrate(vm, dst_pm);

  return 0;
}

static void vm_migrate_async(msg_vm_t vm, msg_host_t dst_pm)
{
  const char *vm_name = MSG_vm_get_name(vm);
  const char *dst_pm_name = MSG_host_get_name(dst_pm);
  msg_host_t host = MSG_host_self();

  const char *pr_name = "mig_wrk";
  char **argv = xbt_new(char *, 4);
  argv[0] = xbt_strdup(pr_name);
  argv[1] = xbt_strdup(vm_name);
  argv[2] = xbt_strdup(dst_pm_name);
  argv[3] = NULL;

  MSG_process_create_with_arguments(pr_name, migration_worker_main, NULL, host, 3, argv);
}

/**
 * Used to simulate the cpu load per VM.
 */
static int worker_busy_loop_main(int argc, char *argv[])
{
  msg_task_t *task = MSG_process_get_data(MSG_process_self());
  for (;;)
    MSG_task_execute(*task);

  return 0;
}

static int master_main(int argc, char *argv[])
{
  xbt_dynar_t hosts_dynar = MSG_hosts_as_dynar();
  msg_host_t hosts[4];
  msg_host_t temp_host=NULL;
  msg_vm_t vm;
  char *vm_id_str = (char *)calloc(sizeof(char), 100);
  s_vm_params_t params;
  double cpu_speed;
  memset(&params, 0, sizeof(params));
  int i=0;
  int actions_to_do=atoi(argv[1]);
  msg_task_t vm_task;
  msg_process_t vm_process;

  //Get a pointer for each Host
  int host_id=0;
  for(i=0; i<4; i++){
    temp_host = xbt_dynar_get_as(hosts_dynar, i, msg_host_t);
    cpu_speed = MSG_get_host_speed(temp_host);
    if(!strcmp("south", MSG_host_get_name(temp_host))){
      //Assure that south is associated with id 0
      hosts[0] = temp_host;
      host_id=0;
    }else if(!strcmp("west", MSG_host_get_name(temp_host))){
      //Assure that west is associated with id 1
      hosts[1] = temp_host; 
      host_id=1;
    }else if(!strcmp("north", MSG_host_get_name(temp_host))){
      //Assure that north is associated with id 2
      hosts[2] = temp_host; 
      host_id=2;
    }else if(!strcmp("east", MSG_host_get_name(temp_host))){
      //Assure that east is associated with id 3
      hosts[3] = temp_host; 
      host_id=3;
    }
    XBT_INFO("===******>>>> HOST (%s) %d : speed %f\n", MSG_host_get_name(hosts[host_id]), host_id, cpu_speed);
  } 

  for(i=2; i< (actions_to_do*5); i+=5){

    int
      param_action_vm    = atoi(argv[i]),
      param_action_start = atoi(argv[i+1]),
      param_action_end   = atoi(argv[i+2]),
      param_action_from  = atoi(argv[i+3]),
      param_action_to    = atoi(argv[i+4]);

    //In the first time start action on the precise time given as parameter
    if(i==2){
      MSG_process_sleep(param_action_start);
    }
    
    //Create a new VM to migrate on the specific host given as parameter
    sprintf(vm_id_str, "VM%d", param_action_vm);

    //Set ram size to 2 GB or 3 GB according to the VM id.
    if(param_action_vm < 5){
      params.ramsize = 2 * 1024; // 2 GB
    }else{
      params.ramsize = 3 * 1024; // 3 GB
    }

    // Set the maximal BW and dirty page intensity depending on the nodes NIC capacity
    int maxBW, dirtyPagesIntensity;
    if(param_action_from == 0 || param_action_from == 1 || 
       param_action_to == 0 || param_action_to == 1) { 
      maxBW = 63;
      dirtyPagesIntensity = 64;
      //dirtyPagesIntensity = 80;
    }
    else {          
      maxBW = 125;  
      dirtyPagesIntensity = 32;
      //dirtyPagesIntensity = 40;
    }

    //Create the VM with a detailed speed
    vm = MSG_vm_create(hosts[param_action_from], vm_id_str,
		       //number of cpus
		       1,
		       //memory size in MB
		       params.ramsize,
		       //netcap maximal bandwidth in MBytes/s
		       125,
           //maxBW,
		       //unsed disk_path
		       NULL,
		       //unused disk_size,
		       0,
		       //migration speed in MBytes/s must be < netcap
		       125,
           //maxBW,
		       //dirty page intencity, works as percentage of migration speed
           //dirtyPagesIntensity
           5 
    );


    //Start the VM
    MSG_vm_start(vm);

    //Set load on each VM at maximum and use bound to fit a single core
    vm_task = MSG_task_create(vm_id_str, 1e11, 0, NULL);
    vm_process = MSG_process_create(vm_id_str, worker_busy_loop_main, &vm_task, vm);
    MSG_vm_set_bound(vm, cpu_speed/8);
  
    //Migrate VM
    double mig_sta = MSG_get_clock();
    vm_migrate(vm, hosts[param_action_to]);
    double mig_end = MSG_get_clock();
    MSG_vm_destroy(vm);

    sem_wait(&gl_output_file_sem);
    fprintf(gl_output_file, " %d %d %d %d %d %lf %lf\n",
	    param_action_vm,
	    param_action_start,
	    param_action_end,
	    param_action_from,
	    param_action_to,
	    mig_sta,
	    mig_end
	    );
    sem_post(&gl_output_file_sem);
  }
  
  return 0;
}

static void launch_master(msg_host_t host, int argc, char **argv)
{
  char *pr_name;
  pr_name = (char *) calloc(sizeof(char), 100);
  sprintf(pr_name, "master_%s", argv[1]) ;
  MSG_process_create_with_arguments(pr_name, master_main, NULL, host, argc, argv);
}

/* 
 * Function used to compare two actions, if actions p1 starts before
 * action p2 it returns -1, if they both start on the same time returns
 * 0, otherwise return +1.
 *
 */
static int compare_actions(const void *p1, const void*p2){
  Action *a1 = (Action *) p1;
  Action *a2 = (Action *) p2;

  if(a1->start < a2->start)
    return -1;
  else if(a1->start == a2->start)
    return 0;
  else
    return +1;
}


int main(int argc, char *argv[])
{
  json_formated_input json_parsed;
  char **param_argv;
  int i=0;
  int j=0;
  int return_code=0;
  int precedence[10];
  
  /* Verify command line arguments */
  if(argc != 4){
    perror("Need 3 arguments, topology file, output file and BTRPlace trace as input!");
    printf("Usage:'\n");
    printf("\t./migrate_vm <topology_file> <output_file> <btrplace_json_input_file>\n\n");
    printf("Example:\n");
    printf("\t./migrate_vm topology.xml random.1.output ../json-parser/random.1.json\n\n");
    exit(1);
  }

  /* Use an output file for this run */
  gl_output_file = fopen(argv[2], "w");
  if(gl_output_file == NULL){
    perror("Erro opening output file file!");
    printf("Check if file %s exist in the current path.\n", argv[2]);
    exit(1);
  }
  
  sem_init(&gl_output_file_sem, 0, 1);

  /* Put header on file */
  sem_wait(&gl_output_file_sem);
  fprintf(gl_output_file, " vm start end from to actualStart actualEnd\n");
  sem_post(&gl_output_file_sem);

  /* Get the arguments */
  MSG_init(&argc, argv);

  /* load the platform file */
  MSG_create_environment(argv[1]);

  /* Parse the json input file */
  parse_json_vms(argv[3], &json_parsed);

  /*
typedef struct struct_Action {
  uint vm;
  uint start;
  uint end;
  uint from;
  uint to;
}Action;

typedef struct struct_json_formated_input {
  VM vms[10];
  Action actions[10];
}json_formated_input;
  */

  /**************************************
   * Algorithm to compute a precedence for the migrations 
   **************************************/
  //First initialize the prececence array with -1
  //Sort actions by start time
  qsort(json_parsed.actions, json_parsed.total_actions, sizeof(Action), compare_actions);
    printf("====>ACTIONS<====\n");
  for (i = 0; i < json_parsed.total_actions; i++) {
    printf("\tVM id => %d\n", json_parsed.actions[i].vm);
    printf("\t      => start => %d\n", json_parsed.actions[i].start);
    printf("\t      => end => %d\n", json_parsed.actions[i].end);
    printf("\t      => from => %d\n", json_parsed.actions[i].from);
    printf("\t      => to => %d\n", json_parsed.actions[i].to);
  }

  for(i=0; i<json_parsed.total_actions; i++){
    precedence[i] = -1;
  }
  for(i=0; i<json_parsed.total_actions; i++){
    int current_node=i;
    /* Search for precedence */
    for(j=current_node; j<json_parsed.total_actions; j++){
      //Check if this action is already precedence of another
      //if not it have to compute precedence 
      if(precedence[j] == -1){
	//If find a precedence, i.e. j start depends on end of  current_node
	if(json_parsed.actions[j].start == json_parsed.actions[current_node].end){
	  //Mark the precedence array, i.e. j depends on current
	  precedence[j] = i;
	  //Current_node must jump to j, to avoid multiple dependencies on same task
	  current_node = j;
	  //j restart from there
	  j = current_node;
	}
      }
    }
    if(precedence[i]==-1){
      precedence[i]=i;
    }
  }
  //For debug proposes print the precedence array
  printf("\n\n****++++++=====>>>>> PRECEDENCE ARRAY :\n\t{");
  for(i=0; i<json_parsed.total_actions-1; i++){
    printf("%d, ", precedence[i]);
  } 
  printf("%d}\n\n", precedence[i]);
  int count_threads=0;
  //find the maximum on precedence array as a way of counting how many threads
  //are needed
  for(i=0; i<json_parsed.total_actions; i++){   
    if(count_threads < precedence[i]){
      count_threads = precedence[i];
    }
  }

  /**************************************
   * Create on thread per group of actions
   **************************************/
  for(i=0;i<=count_threads;i++){
    /* Instantiate enough space for parameters on argv max of 100 params */
    int argv_counter=1;
    param_argv = (char **) calloc(sizeof(char *), 100);
    for(j=0; j<100; j++){
      param_argv[j] = (char *) calloc(sizeof(char), 100);
    }

    //Compute how many actions are supposed to be assigned to this thread
    int count_actions=0;
    for(j=0; j<json_parsed.total_actions; j++){
      if(precedence[j] == i){
	count_actions++;
      }
    }

    //Number of actions assigned  to the current thread
    sprintf(param_argv[argv_counter++], "%d", count_actions);

    //For each action that is assigned to current thread produce argv parameters
    for(j=0; j<json_parsed.total_actions; j++){
      if(precedence[j] == i){
	sprintf(param_argv[argv_counter++], "%d", json_parsed.actions[j].vm);
	sprintf(param_argv[argv_counter++], "%d", json_parsed.actions[j].start);
	sprintf(param_argv[argv_counter++], "%d", json_parsed.actions[j].end);
	sprintf(param_argv[argv_counter++], "%d", json_parsed.actions[j].from);
	sprintf(param_argv[argv_counter++], "%d", json_parsed.actions[j].to);
      }
    }

    xbt_dynar_t hosts_dynar = MSG_hosts_as_dynar();
    msg_host_t pm0 = xbt_dynar_get_as(hosts_dynar, 0, msg_host_t);
    launch_master(pm0, argv_counter, param_argv);

  }
 
  int res = MSG_main();

  return_code = fclose(gl_output_file);
  if(return_code != 0){
    perror("Cannot close file!");
  }
  
  return !(res == MSG_OK);
}
