const fs = require('fs');
const path = require('path');

function replaceInFile(filePath) {
  if (!fs.existsSync(filePath)) return;
  let content = fs.readFileSync(filePath, 'utf8');
  let original = content;
  
  content = content.replace(/['""]([.\/a-zA-Z0-9_-]+)\/api\/client['""]/g, (match, p1) => ' + p1 + /services/api.client');
  content = content.replace(/['""]([.\/a-zA-Z0-9_-]+)\/contexts\/AuthContext['""]/g, (match, p1) => ' + p1 + /app/AuthContext');
  
  if (content !== original) {
    fs.writeFileSync(filePath, content);
  }
}

function walkDir(dir) {
  fs.readdirSync(dir).forEach(f => {
    let dirPath = path.join(dir, f);
    let isDir = fs.statSync(dirPath).isDirectory();
    if (isDir) {
      walkDir(dirPath);
    } else if (f.endsWith('.tsx') || f.endsWith('.ts')) {
      replaceInFile(dirPath);
    }
  });
}

walkDir('C:/AI START UP/frontend/src');
