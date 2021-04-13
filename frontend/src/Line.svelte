<script>
    import { createEventDispatcher } from 'svelte';
    import Word from "./Word.svelte";
    import IconButton from "./IconButton.svelte";
    import { push, remove } from "./common/arrays";

    export let words = [];
    export let editableIndex = -1;

    const title = 'Удалить строку';
    const REMOVE_DELAY = 150;
    const URL = "http://localhost:8082/getRhymes"

    $: rhymes = [];

    const dispatch = createEventDispatcher();

    const onRemoveLine = () => {
        dispatch('removeLine');
    };

    const onAddLine = () => {
        dispatch('addLine');
    };

    const onEnter = (e, i) => {
        console.log('enter')
        const word = e.detail.word;
        if (word) {
            editableIndex = i + 1;
            words[i] = e.detail.word;
        } else {
            console.log('rm entr', e, i)
            editableIndex = i - 1;
            onRemoveWord(i);
        }
        if (i === words.length - 1) {
            onAddLine();
        }
    };

    const onPunct = (e, i) => {
        editableIndex = i;
        words = push(words, ++editableIndex, e.detail.sign);
        words = push(words, ++editableIndex, '');
        addWord(e, i);
    };

    const onSpace = (e, i) => {
        editableIndex = i;
        words = push(words, ++editableIndex, '');
        addWord(e, i);
    };

    const addWord = (e, i) => {
        const word = e.detail.word;
        // if (word) {
        //     words[i] = word;
        // } else {
        //     onRemoveWord(i);
        // }
        words[i] = word;
    };

    const onRemoveWord = i => {
        setTimeout(() => {
            console.log('rm bef', words)
            remove(words, i);
            console.log('rm aft', words)
            words = words;
        }, REMOVE_DELAY);
    };

    const onBack = (e, i) => {
        console.log('bk', e, i)
        if (i > 0) {
            editableIndex = i - 1;
        } else {
            dispatch('back');
        }
    };

    const onNext = (e, i) => {
        if (i < words.length - 1) {
            console.log('nxt',i + 1)
            editableIndex = i + 1;
        } else {
            dispatch('next');
        }
    };

    const onGetRhymes = async (e, i) => {
        const word = words[i];
        let response = await fetch(`${URL}?word=${word}`);
        if (response.ok) {
            rhymes = await response.json();
        } else {
            console.log("Ошибка HTTP: " + response.status);
        }
    };
</script>

<div class="line">
    <div>
        {#each words as word, i}
            <Word
                    {word}
                    editable={editableIndex === i}
                    on:removeWord={() => onRemoveWord(i)}
                    on:enter={e => onEnter(e, i)}
                    on:punct={e => onPunct(e, i)}
                    on:back={e => onBack(e, i)}
                    on:next={e => onNext(e, i)}
                    on:space={e => onSpace(e, i)}
                    on:editFinished={e => onGetRhymes(e, i)}/>
        {/each}
    </div>

    <div class="right-menu">
        <IconButton icon="cross" size="10" {title} on:click={onRemoveLine}/>
        <IconButton icon="plus" size="11" {title} on:click={onAddLine}/>
        <!--{#if rhymes.length}-->
        <!--    <select name="rhymes">-->
        <!--        {#each rhymes as rhyme}-->
        <!--            <option value={rhyme}>{rhyme}</option>-->
        <!--        {/each}-->
        <!--    </select>-->
        <!--{/if}-->
        <button on:click={onGetRhymes}>Get rhyme</button>
    </div>
</div>

<style lang="scss">
    .line {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0.25rem;

        .right-menu {
            display: flex;
            align-items: center;
            float: right;
            margin-left: 1rem;
            opacity: 0;
        }

        &:hover, &:focus, &:focus-within {
            background-color: #EEEE;

            .right-menu {
                opacity: 1;
            }
        }
    }
</style>