import { getRoute } from "./routes";

export async function getRhymes(word, stressPos) {
    console.log(stressPos)
    if (typeof stressPos !== 'undefined') {
        word = word.substr(0, stressPos + 1) + '\'' + word.substr(stressPos + 1);
    }
    let response = await fetch(`${getRoute('getRhymes')}?word=${word}`);
     if (response.ok) {
         return await response.json();
     } else {
         console.log("Ошибка HTTP: " + response.status);
         return {
             error: response.status
         }
     }
}