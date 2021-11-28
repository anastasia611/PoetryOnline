import App from './Main.svelte';
import { parseLine, parseStanzas } from "./common/strings";
import { getComments } from "../stuff/vk";

const rawText = `Молчи , скрывайся и таи
И чувства и мечты свои —
Пускай в душевной глубине
Встают и заходят оне
Безмолвно , как звезды в ночи , —
Любуйся ими — и молчи .

Как сердцу высказать себя ?
Другому как понять тебя ?
Поймет ли он , чем ты живешь ?
Мысль изреченная есть ложь —
Взрывая , возмутишь ключи ,
Питайся ими — и молчи . . .

Лишь жить в себе самом умей —
Есть целый мир в душе твоей
Таинственно-волшебных дум —
Их оглушит наружный шум ,
Дневные разгонят лучи —
Внимай их пенью — и молчи ! . .`;

const stanzas = parseStanzas(rawText);

const app = new App({
    target: document.body,
    props: {
        stanzas
    }
});

const groups = {
    lentach: 29534144,
    'mash.moyka': 179189462,
    science: 29559271
};

// getComments(
//     [],
//     20,
//     0,
//     300);

export default app;