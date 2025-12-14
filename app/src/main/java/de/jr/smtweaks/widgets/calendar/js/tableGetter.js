(function() {

class Item {
  constructor(leftTop, rightTop, rightTopAlternate, bottom, bottomAlternate, isCancelled, row, col) {
    this.leftTop = leftTop;
    this.rightTopAlternate = rightTopAlternate;
    this.rightTop = rightTop;
    this.bottom = bottom;
    this.bottomAlternate = bottomAlternate;
    this.isCancelled = isCancelled;
    this.row = row;
    this.col = col;
  }
}


function main() {
let array = [];
  const table = document.querySelector("table.calendar-table");
  for (let row = 1; row < table.rows.length; row++) {
    for (let col = 1; col < table.rows[row].cells.length; col++) {
      const selection = table.rows[row].cells[col];
      let isCancelled = false;
      if (selection.innerHTML.includes("lesson-cell cancelled")) {
        isCancelled = true;
      }
      let parts = selection.innerText.trim().split("\n");
      if (parts.length < 3) {
        continue;
      }
      let leftTop = parts[0];
      let rightTop = parts[1];
      let rightTopAlternate = null;
      if (parts[1].includes(") ")) {
        rightTopAlternate = parts[1].split(") ")[1].trim();
        rightTop = parts[1].split(") ")[0] + ")".trim();
      }
      let bottom = parts[2];
      let bottomAlternate = null;
      if (parts[2].includes(") ")) {
        bottomAlternate = parts[2].split(") ")[1].trim();
        bottom = parts[2].split(") ")[0] + ")".trim();
      }
      array.push(new Item(leftTop, rightTop, rightTopAlternate, bottom, bottomAlternate, isCancelled, row, col))
    }
  }
  return array;
}

return JSON.stringify(main())})()