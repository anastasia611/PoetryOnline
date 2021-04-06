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

    let invisible = true;

    const dispatch = createEventDispatcher();

    const onRemove = () => {
        dispatch('removeLine');
    };

    const onAddLine = () => {
        dispatch('addLine');
    };

    const onAddWord = (e, i) => {
        const newWord = e.detail.newWord;
        const word = e.detail.word;

        if (newWord) {
            console.log('k', newWord)
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
</script>

<div class="line"
     on:mouseover={() => invisible = false}
     on:mouseleave={() => invisible = true}>

    <div>
        {#each wordsData as word, i}
            <Word word={word.word} editable={word.editable}
                  on:removeWord={() => removeWord(i)}
                  on:addWord={e => onAddWord(e, i)}/>
        {/each}
    </div>

    <div class="right-menu" class:invisible>
        <RemoveButton size="10" {title} on:click={onRemove}/>
        <AddButton on:click={onAddLine}/>
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