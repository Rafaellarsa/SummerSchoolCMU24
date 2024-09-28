import json
import csv

# Path to the input and output files
input_file = 'output.json'
output_file = 'codestate_kc.csv'

# Open the input file and read the JSON strings
with open(input_file, 'r') as infile:
    data = json.load(infile)

# Open the CSV file for writing
with open(output_file, 'w', newline='') as csvfile:
    csvwriter = csv.writer(csvfile)
    # Write the header
    csvwriter.writerow(['KC', 'CodeStateID'])

    # Iterate over each item in the list (each item is a JSON string)
    for item in data:
        # Parse the string as a JSON object
        parsed_item = json.loads(item)
        
        # Extract the 'concept' and 'id'
        concept = parsed_item['concept']
        codestate_id = parsed_item['id']
        
        # Write the row to the CSV
        csvwriter.writerow([concept, codestate_id])

print(f"Data successfully written to {output_file}")