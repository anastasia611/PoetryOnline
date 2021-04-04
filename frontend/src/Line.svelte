<script>
    import {createEventDispatcher} from 'svelte';
    import Word from "./Word.svelte";
    import RemoveIcon from "./RemoveButton.svelte";
    import AddButton from "./AddButton.svelte";
    import {remove} from "./common/arrays";

    export let words = [];

    const title = 'Удалить строку';
    let invisible = true;
    const REMOVE_DELAY = 150;

    const dispatch = createEventDispatcher();

    const onRemove = () => {
        dispatch('removeLine');
    };

    const onAdd = () => {
        dispatch('addLine');
    };

    const removeWord = i => {
        setTimeout(() => {
            remove(words, i);
            words = words;
        }, REMOVE_DELAY);
    };
</script>

<div class="line"
     on:mouseover={() => invisible = false}
     on:mouseleave={() => invisible = true}>

    <div>
        {#each words as word, i}
            <Word {word} on:removeWord={() => removeWord(i)}/>
        {/each}
    </div>

    <div class="right-menu" class:invisible>
        <RemoveIcon size="10" {title} on:click={onRemove}/>

      <AddButton on:click={onAdd}/>
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