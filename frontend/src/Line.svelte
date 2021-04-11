<script>
    import { createEventDispatcher } from 'svelte';
    import Word from "./Word.svelte";
    import IconButton from "./IconButton.svelte";
    import { push, remove } from "./common/arrays";
    import { isPunctuationMark } from "./common/strings";

    export let words = [];
    export let editableIndex = -1;

    const title = 'Удалить строку';
    const REMOVE_DELAY = 150;
    const URL = "http://localhost:8082/getRhymes"

    $: rhymes = [];
    let invisible = true;

    const dispatch = createEventDispatcher();

    const onRemoveLine = () => {
        dispatch('removeLine');
    };

    const onAddLine = () => {
        dispatch('addLine');
    };

    const onEnter = (e, i) => {
        const word = e.detail.word;
        if (word) {
            editableIndex = i + 1;
            words[i] = e.detail.word;
        } else {
            editableIndex = i - 1;
            onRemoveWord(i);
        }
        if (i === words.length - 1) {
            onAddLine();
        }
    };

    const onPunct = (e, i) => {
        const newWord = e.detail.sign;
        const word = e.detail.word;

        if (newWord) {
            words = push(words, i + 1, newWord);
            if (!isPunctuationMark(newWord)) {
                editableIndex = i + 1;
            }
        }

        if (word) {
            words[i] = e.detail.word;
        } else {
            onRemoveWord(i);
        }
    };

    const onRemoveWord = i => {
        setTimeout(() => {
            console.log(i, words)
            remove(words, i);
            console.log(i, words)
            words = words;
        }, REMOVE_DELAY);
    };

    const onBack = (e, i) => {
        console.log('back', i)
        if (i > 0) {
            editableIndex = i - 1;
        } else {
            dispatch('back');
        }
    };

    const onNext = async (e, i) => {
        if (i < words.length - 1) {
            editableIndex = i + 1;
        } else {
            dispatch('next');
        }
    };

    const onGetRhymes = async (e, i) => {
        const word = words[i];
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
        {#each words as word, i}
            <Word word={word}
                  editable={editableIndex === i}
                  on:removeWord={() => onRemoveWord(i)}
                  on:enter={e => onEnter(e, i)}
                  on:punct={e => onPunct(e, i)}
                  on:back={e => onBack(e, i)}
                  on:next={e => onNext(e, i)}
                  on:editFinished={e => onGetRhymes(e, i)}/>
        {/each}
    </div>

    <div class="right-menu" class:invisible>
        <IconButton icon="cross" size="10" {title} on:click={onRemoveLine}/>
        <IconButton icon="plus" size="10" {title} on:click={onAddLine}/>
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

        &:focus-within {
            opacity: 1;
        }
    }
</style>