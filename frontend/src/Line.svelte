<script>
    import {createEventDispatcher} from 'svelte';
    import Word from "./Word.svelte";
    import RemoveButton from "./RemoveButton.svelte";
    import AddButton from "./AddButton.svelte";
    import {push, remove} from "./common/arrays";
    import {isPunctuationMark} from "./common/strings";

    export let words = [];

    $: wordsData = words.map(w => ({ word: w, editable: false }));

    const title = 'Удалить строку';
    const REMOVE_DELAY = 150;
    const URL = "http://localhost:8082/getRhymes"

    $: rhymes = [];
    let invisible = true;

    const dispatch = createEventDispatcher();

    const onRemove = () => {
        dispatch('removeLine');
    };

    const onAddLine = () => {
        dispatch('addLine');
    };

    const onEnter = (e, i) => {
        const word = e.detail.word;
        if (word) {
            wordsData[i] = { word: e.detail.word, editable: false };
        } else {
            removeWord(i);
        }
        if (i === words.length - 1) {
            onAddLine();
        }
    };

    const onPunct = (e, i) => {
        const newWord = e.detail.newWord;
        const word = e.detail.word;

        if (newWord) {
            let editable = !isPunctuationMark(newWord);
            wordsData = push(wordsData, i + 1, { word: newWord === ' ' ? '' : newWord, editable });
        }

        if (word) {
            wordsData[i] = { word: e.detail.word, editable: false };
        } else {
            removeWord(i);
        }
    };

    const removeWord = i => {
        setTimeout(() => {
            remove(wordsData, i);
            wordsData = wordsData;
        }, REMOVE_DELAY);
    };

    const onGetRhymes = async (e, i) => {
        const word = wordsData[i].word;
        let response = await fetch(`${URL}?word=${word}`);
console.log(word)
        if (response.ok) {
            rhymes = await response.json();
        } else {
            console.log("Ошибка HTTP: " + response.status);
        }
    };
</script>

<div class="line"
     on:mouseover={() => invisible = false}
     on:mouseleave={() => invisible = true}>

    <div>
        {#each wordsData as word, i}
            <Word word={word.word} editable={word.editable}
                  on:removeWord={() => removeWord(i)}
                  on:enter={e => onEnter(e, i)}
                  on:punct={e => onPunct(e, i)}
                  on:editFinished={e => onGetRhymes(e, i)}/>
        {/each}
    </div>

    <div class="right-menu" class:invisible>
        <RemoveButton size="10" {title} on:click={onRemove}/>
        <AddButton on:click={onAddLine}/>
        {#if rhymes.length}
            <select name="rhymes">
                {#each rhymes as rhyme}
                    <option value={rhyme}>{rhyme}</option>
                {/each}
            </select>
        {/if}
        <button on:click={onGetRhymes}>Get rhyme</button>
    </div>
</div>

<style lang="scss">
    .line {
        display: flex;
        align-items: center;
        justify-content: space-between;

        &:hover, &:focus {
            background-color: #EEEE;
        }

        padding: 0.25rem;
    }

    .right-menu {
        display: flex;
        align-items: center;
        float: right;
        margin-left: 1rem;

        &.invisible {
            opacity: 0;
        }
    }
</style>