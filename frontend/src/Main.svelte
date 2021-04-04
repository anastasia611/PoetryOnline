<script>
    import {isLetter, isPunctuationMark} from "./common/strings";
    import Stanza from "./Stanza.svelte";

    export let stanzas = [];

    let newWord = '';

    const onKeyPress = e => {
        if (e.key === "Enter" || e.key === " " || isPunctuationMark(e.key)) {
            setTimeout(() => {
                if (newWord) {
                    stanzas.push(newWord);
                }
                if (isPunctuationMark(e.key)) {
                    stanzas.push(e.key);
                }
                stanzas = stanzas;
                newWord = '';
            }, 100);
        } else if (isLetter(e.key)) {
            const pos = e.target.selectionStart;
            newWord = newWord.substr(0, pos) + e.key + newWord.substr(pos, newWord.length);
        } else if (e.key === 'Backspace' || e.key === 'Delete') {
            newWord = newWord.substr(0, newWord.length - 1);
        } else if (e.key === 'ArrowLeft') {
            const pos = e.target.selectionStart - 1;
            e.target.setSelectionRange(pos, pos);
        } else if (e.key === 'ArrowRight') {
            const pos = e.target.selectionStart + 1;
            e.target.setSelectionRange(pos, pos);
        }
    };
</script>

<main>
    <div class="container" id="container1">
        <div class="poem">
            <h1>Стихотворение</h1>
            {#each stanzas as lines}
               <Stanza {lines} />
            {/each}
            <div class="word-input">
                <label for="word"></label>
                <input
                        type="text" autocomplete="on" autocapitalize="off" spellcheck="true"
                        class="word-input" id="word" maxlength="32" placeholder="введите слово.."
                        on:keydown|preventDefault={onKeyPress}
                        value={newWord}>
            </div>
        </div>
    </div>
</main>

<style lang="scss">
    main {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 1em;
        margin: 0 auto;
    }

    h1 {
        color: #DE8484;
    }

    .container {
        /*padding: 0 5rem;
        display: inline-block;*/
        width: 75%;
        background-color: white;

    }

    .poem {
        display: flex;
        width: fit-content;
        flex-direction: column;
        margin-left: auto;
        margin-right: auto;
        padding: 1rem;
    }

    .word-input {
        display: inline-block;

        & input {
            height: 2rem;
            margin-left: 0.5rem;
            border: 1px solid white;

            &:focus {
                outline: none;
                border: 1px solid #cccccc;
            }
        }
    }

</style>
