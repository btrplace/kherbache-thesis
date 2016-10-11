#ifndef json_parser_h
#define json_parser_h
/**
 * Parse the files from BTRPlace to find the VMs configuration, initial state, and migrations
 * to trigger. To understand more about the goal of this program please check the publication
 * XXX
 *
 * Depends on json-c, from https://github.com/json-c/json-c/
 *
 * Compiling example:
 *  clang -I/usr/local/Cellar/json-c/0.12/include -L/usr/local/Cellar/json-c/0.12/lib main.c -ljson-c -o json-parser
 *
 * Running example:
 *   ./json-parser random.1.json
 *
 * The topology used on tests assume the following network.
 *
 * host # 0 ---500 Mb/s-----.   .--- 1 Gb/s ----- host # 2
 *                           \ /
 *                    network X switch
 *
 * host # 1 ---500 Mb/s----./   \.--- 1 Gb/s ----- host # 3
 *
 * @author Pedro Velho
 * @date 18/12/2015
 */
#include "json-c/json.h"
#include <string.h>
#include <stdio.h>

typedef struct struct_VM {
  uint ram;
  uint init_host;
}VM;

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
  int total_actions;
}json_formated_input;

/**
 * Print the usage of this program.
 *
 * @param program_name a string with the program name.
 */
void print_usage(char program_name[]);

/**
 * Parse a json file use the json_input struct as output parameter.
 *
 */
void parse_json_vms(char json_input_filename[], json_formated_input *json_parsed);

#endif /* json_parser_h */
