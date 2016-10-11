
#include "json-parser.h"

int main(int argc, char **argv) {
  json_formated_input *result;
    
  result = (json_formated_input *)calloc(sizeof(struct struct_json_formated_input), 1);
  if(argc != 2){
    perror("Expected a single parameter for the input file!\n");
    print_usage(argv[0]);
    exit(1);
  }
    
  parse_json_vms(argv[1], result);
  return 0;
}
