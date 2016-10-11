//
//  json-parser.c
//  json-parser
//
//  Created by Pedro Velho on 12/22/15.
//  Copyright © 2015 Pedro Velho. All rights reserved.
//

#include "json-parser.h"

/**
 * Search for a given name of object inside the json. Search rescursively for now can
 * do it without recurssion on the future for better memory management.
 *
 * @param name string with the key name of the searched object.
 * @param root JSON object root to start searching for.
 * @param return_obj pointer to the found object if so, NULL other wise.
 */
json_bool find_obj_by_name(char name[], json_object *root, json_object **return_obj){
  enum json_type type;
  json_bool found = FALSE;
  json_object *temp_obj = NULL;
  int i;
  int arraylen;
    
  //Set it to null to indicate when object searched for is not found in JSON.
  *return_obj = NULL;
    
  //Retrieve the type of this object and act accordinly.
  type = json_object_get_type(root);
    
  //Treat each type as a separate case. In c-json struct json_object is never defined
  //hope to figure that out on the future.
  switch (type) {
  case json_type_null:
    return FALSE;
    break;
  case json_type_boolean:
    break;
  case json_type_double:
    break;
  case json_type_int:
    break;
  case json_type_object: ; //This empty instruction avoids parsing error : expected expression.
    json_object_object_foreach(root, key, val) {
      if(root == NULL){
	return FALSE;
      }else if(strcmp(key,name)==0){
	*return_obj = val;
	return TRUE;
      }else{
	found = find_obj_by_name(name, (json_object *)val, return_obj);
	if(found){
	  return found;
	}
      }
    }
    break;
  case json_type_array: ; //This empty instruction avoids parsing error : expected expression.
    array_list* array = json_object_get_array(root);
    if(array->length == 0){
      return FALSE;
    }else{
      arraylen = json_object_array_length(root);
      for (i = 0; i < arraylen; i++) {
	temp_obj = json_object_array_get_idx(root, i);
	found = find_obj_by_name(name, (json_object *)temp_obj, return_obj);
	if(found){
	  return found;
	}
      }
    }
    break;
  case json_type_string:
    break;
  }
  return FALSE;
}

/**
 * Print the usage of this program.
 *
 * @param program_name a string with the program name.
 */
void print_usage(char program_name[]){
    printf("Usage:\n");
    printf("\t%s\n\n", program_name);
}

/**
 * Parse a json file use the json_input struct as output parameter.
 *
 */
void parse_json_vms(char json_input_filename[], json_formated_input *json_parsed) {
  FILE *json_input = NULL;
  char *string;
  size_t max_bytes = 0;
  json_object * temp_obj;
  json_object * temp_array_obj;
  json_object * root_obj;
  int i;
  int arraylen;
    
  // Read the json input file.
  json_input = (FILE *) fopen(json_input_filename, "r");
    
  // Allocate a big enough string to hold the json line stored on file.
  string = (char *)calloc(99999, sizeof(char));
    
  //It will allocate space if string is NULL and max_bytes points to 0.
  getline(&string, &max_bytes, json_input);
    
  root_obj = json_tokener_parse(string);
  
  /****************************************
   Find the memory of each VM.
  ****************************************/
  printf("====>VMs MEMORY<====\n");
  if(!find_obj_by_name("vms", root_obj, &temp_obj)){
    perror("Impossible to find onlineNodes check the JSON input file!\n");
    exit(1);
  }
  json_object_object_foreach(temp_obj, memKey, memVal) {
    printf("VM %s\n", memKey);
    json_object_object_foreach(memVal, memKey2, memVal2) {
      if(strcmp(memKey2, "memUsed")==0){
	json_parsed->vms[atoi(memKey)].ram = json_object_get_int(memVal2)/1000;
	printf("\tTotal memoray %d GB\n", json_object_get_int(memVal2)/1000);
      }
    }
  }
    
    
  /****************************************
   Find and loop onlineNodes to start each VM on a specific location.
  ****************************************/
  if(!find_obj_by_name("onlineNodes", root_obj, &temp_obj)){
    perror("Impossible to find onlineNodes check the JSON input file!\n");
    exit(1);
  }
  json_object *runningVMs_obj = NULL;
  printf("====>INITIAL STATE<====\n");
  json_object_object_foreach(temp_obj, onlineNodesKey, OnlineNodesVal) {
    printf("Host %s\n", onlineNodesKey);
    int hostId = atoi(onlineNodesKey);
    if(!find_obj_by_name("runningVMs", OnlineNodesVal, &runningVMs_obj)){
      perror("Impossible to find runningVms check the JSON input file!\n");
      exit(1);
    }
    printf("\tVMs running :\n");
    arraylen = json_object_array_length(runningVMs_obj);
    for (i = 0; i < arraylen; i++) {
      temp_array_obj = json_object_array_get_idx(runningVMs_obj, i);
      json_parsed->vms[json_object_get_int(temp_array_obj)].init_host = hostId;
      printf("\t\tVM id => %s\n", json_object_get_string(temp_array_obj));
    }
  }
    
    
  /****************************************
   Find and loop to trigger migration of VMs.
  ****************************************/
  if(!find_obj_by_name("actions", root_obj, &temp_obj)){
    perror("Impossible to find actions check the JSON input file!\n");
    exit(1);
  }
  arraylen = json_object_array_length(temp_obj);
  json_parsed->total_actions = arraylen;
  printf("====>ACTIONS<====\n");
  for (i = 0; i < arraylen; i++) {
    temp_array_obj = json_object_array_get_idx(temp_obj, i);
    json_object_object_foreach(temp_array_obj, actionKey, actionVal) {
      if(strcmp(actionKey, "vm") == 0){
	json_parsed->actions[i].vm = json_object_get_int(actionVal); 
	printf("\tVM id => %d\n", json_object_get_int(actionVal));
      }
      if(strcmp(actionKey, "start") == 0){
	json_parsed->actions[i].start = json_object_get_int(actionVal); 
	printf("\t      => start => %d\n", json_object_get_int(actionVal));
      }
      if(strcmp(actionKey, "end") == 0){
	json_parsed->actions[i].end = json_object_get_int(actionVal); 
	printf("\t      => end => %d\n", json_object_get_int(actionVal));
      }
      if(strcmp(actionKey, "from") == 0){
	json_parsed->actions[i].from = json_object_get_int(actionVal); 
	printf("\t      => from => %d\n", json_object_get_int(actionVal));
      }
      if(strcmp(actionKey, "to") == 0){
	json_parsed->actions[i].to = json_object_get_int(actionVal); 
	printf("\t      => to => %d\n", json_object_get_int(actionVal));
      }
    }
  }
}

