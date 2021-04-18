<script>
    import { createEventDispatcher } from 'svelte';
    import Word from "./Word.svelte";
    import IconButton from "./IconButton.svelte";
    import { push, remove } from "./common/arrays";

    export let words = [];
    export let wordIndex = 0;

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

    const onEnter = e => {
        const word = e.detail.word;
        if (word) {
            words[wordIndex++] = e.detail.word;
        } else {
            onRemoveWord(--wordIndex);
        }
        if (wordIndex === words.length - 1) {
            onAddLine();
        }
    };

    const onPunct = e => {
        words = push(words, ++wordIndex, e.detail.sign);
        words = push(words, ++wordIndex, '');
        addWord(e, wordIndex);
    };

    const onSpace = e => {
        if (!words[wordIndex]) {
            return;
        }
        if (wordIndex < words.length - 1 && !words[wordIndex + 1]) {
            return;
        }
        words = push(words, ++wordIndex, '');
        addWord(e);
    };

    const addWord = e => {
        words[wordIndex] = e.detail.word;
    };

    const onRemoveWord = i => {
        setTimeout(() => {
            remove(words, i);
            words = words;
            if (!words.length) {
                onRemoveLine();
            }
        }, REMOVE_DELAY);
    };

    const onBack = () => {
        if (wordIndex > 0) {
            --wordIndex;
        } else {
            dispatch('back');
        }
    };

    const onNext = () => {
        if (wordIndex < words.length - 1) {
            ++wordIndex;
        } else {
            dispatch('next');
        }
    };

    const onUp = () => {
        dispatch('back');
    };

    const onDown = () => {
        dispatch('next');
    };

    const onFocus = i => {
        console.log('word', i);
        wordIndex = i;
        dispatch('focus');
    };

    const onGetRhymes = async () => {
        const word = words[wordIndex];
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
                    editable={i === wordIndex}
                    on:focus={() => onFocus(i)}
                    on:removeWord={() => onRemoveWord(i)}
                    on:enter={onEnter}
                    on:punct={onPunct}
                    on:back={onBack}
                    on:next={onNext}
                    on:up={onUp}
                    on:down={onDown}
                    on:space={onSpace}
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
        padding: 0.25rem 0.5rem;

        .right-menu {
            display: flex;
            align-items: center;
            float: right;
            margin-left: 1rem;
            opacity: 0;
        }

        &:hover, &:focus, &:focus-within {
            background-color: #E5E5E5;

            .right-menu {
                opacity: 1;
            }
        }
    }
</style>