export async function getRhymes(word) {
    let response = await fetch(`${URL}?word=${word}`);
     if (response.ok) {
         return {
             data: response.json()
         };
     } else {
         console.log("Ошибка HTTP: " + response.status);
         return {
             error: response.status
         }
     }
}