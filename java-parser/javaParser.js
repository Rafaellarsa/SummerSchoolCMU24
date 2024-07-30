const groupBy = require('lodash/groupBy');
const { spawn } = require('child_process');
const express = require('express');
const cors = require('cors')

const service = express();

service.use(cors());
service.use(express.json());


//Handle command Line options
let settings = {};
let port = 5500;
let jar = "target/java-parser.jar";

process.argv.forEach(function (val, index, array) {
  switch(val){
    case '-docker':
      settings.docker = true;
      break
    case '-port':
      if(process.argv[index+1]){
        port = process.argv[index+1]
      }else{
        console.log("No port provided, using 5500")
      }
    case '-jar':
      if(process.argv[index+1]){
        jar = process.argv[index+1]
      }else{
        console.log("No jar path provided, target/java-parser.jar")
      }
    default:
      break
  }
});

service.post("/parse_java", (req, res) => {
  const p = new ParseJava(req.body, settings);

  p.runParser()
   .then(payload => res.json(payload))
   .catch(error => res.json({error}))
});

class ParseJava{
  constructor(reqCode, settings) {
    this.reqCode = reqCode;
    this.settings = settings;
  }

  //Creaters proper string from req json array to work with parser
  buildCodeString(){
    let codeString ="";
    
    for(let i = 0; i < this.reqCode.length; i++){
      if(i === 0)
        codeString += "#C" + this.reqCode[i].id;
      else
        codeString += "\n#C" + this.reqCode[i].id;
      
      codeString += "\n" + this.reqCode[i].code;
    }

    codeString += "\nEOF"

    return codeString
  }

  runParser(){
    return new Promise((resolve, reject) => {
      const startTime = Date.now();

      const codeString = JSON.stringify([this.buildCodeString()]);

      let parse;
      if(this.settings.docker){
        parse = spawn('docker-compose', ['exec', '-T', 'java', 'java', '-jar', jar, codeString, 'json']);
      } else {
        parse = spawn('java', ['-jar', jar, codeString, 'json']);
      }

      let output = "";

      parse.stdout.on('data', (data) => {
        output += data;
      });

      parse.stderr.on('data', (data) => {
        console.log("reject stderr");
        reject(data);
      });

      parse.on('close', (code) => {
        try {
          const j = JSON.parse(output.trim()).map(ele => JSON.parse(ele));
          const g = groupBy(j, 'id');

          resolve(g);
        } catch(e){
          console.log("reject json");
          reject(e);
        }

        console.log(`Parsed, took ${Date.now() - startTime} miliseconds`)
      });
    });
  }
}



service.listen(port);
console.log("Now listening on " + port);